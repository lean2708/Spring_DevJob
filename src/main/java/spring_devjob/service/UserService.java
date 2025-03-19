package spring_devjob.service;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.RoleEnum;
import spring_devjob.dto.request.UserCreationRequest;
import spring_devjob.dto.request.UserUpdateRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.*;
import spring_devjob.repository.history.UserHistoryRepository;
import spring_devjob.repository.relationship.UserHasRoleRepository;
import spring_devjob.service.relationship.SavedJobService;
import spring_devjob.service.relationship.UserHasRoleService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final ResumeRepository resumeRepository;
    private final ReviewRepository reviewRepository;
    private final PageableService pageableService;
    private final UserHasRoleRepository userHasRoleRepository;
    private final UserHasRoleService userHasRoleService;
    private final SavedJobService savedJobService;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final UserHistoryRepository userHistoryRepository;


    public UserResponse create(UserCreationRequest request){
        checkUserExistenceAndStatus(request.getEmail(), request.getPhone());

        User user = userMapper.userCreationToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(request.getCompanyId() != null){
            companyRepository.findById(request.getCompanyId()).ifPresent(user::setCompany);
        }
        userRepository.save(user);

        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            Set<Role> roleSet = roleRepository.findAllByIdIn(request.getRoleIds());

            Set<UserHasRole> userRoles = roleSet.stream()
                    .map(role -> new UserHasRole(user, role))
                    .collect(Collectors.toSet());

            user.setRoles(new HashSet<>(userHasRoleRepository.saveAll(userRoles)));
        } else{
            user.setRoles(Set.of(userHasRoleService.saveUserHasRole(user, RoleEnum.USER)));
        }

       return userMapper.toUserResponse(user);
    }

    private void checkUserExistenceAndStatus(String email, String phone){
        if(userRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.existsByPhone(phone)){
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        if(userRepository.existsUserInactiveByEmail(email, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_LOCKED);
        }
        if(userRepository.existsUserInactiveByPhone(email, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.PHONE_LOCKED);
        }
        if(userHistoryRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.EMAIL_ARCHIVED_IN_HISTORY);
        }
        if(userHistoryRepository.existsByPhone(phone)){
            throw new AppException(ErrorCode.PHONE_ARCHIVED_IN_HISTORY);
        }
    }

    public UserResponse fetchUserById(long id){
        User userDB = findActiveUserById(id);

        return userMapper.toUserResponse(userDB);
    }

    public PageResponse<UserResponse> fetchAllUsers(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<User> userPage = userRepository.findAll(pageable);

        return PageResponse.<UserResponse>builder()
                .page(userPage.getNumber() + 1)
                .size(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalItems(userPage.getTotalElements())
                .items(userMapper.toUserResponseList(userPage.getContent()))
                .build();
    }

    @Transactional
    public UserResponse update(long id, UserUpdateRequest request){
        User userDB = findActiveUserById(id);

        userMapper.updateUser(userDB, request);

        if(request.getCompanyId() != null){
            companyRepository.findById(request.getCompanyId()).ifPresent(userDB::setCompany);
        }

        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            Set<Role> roleSet = roleRepository.findAllByIdIn(request.getRoleIds());

            userHasRoleRepository.deleteByUser(userDB);

            Set<UserHasRole> userRoles = roleSet.stream()
                    .map(role -> new UserHasRole(userDB, role))
                    .collect(Collectors.toSet());

            userDB.setRoles(new HashSet<>(userHasRoleRepository.saveAll(userRoles)));
        }
        return userMapper.toUserResponse(userRepository.save(userDB));
    }

    @Transactional
    public void delete(long id){
        User userDB = findActiveUserById(id);

        deactivateUser(userDB);

        userRepository.save(userDB);
    }

    private void deactivateUser(User user) {
        resumeRepository.updateAllResumesByUserId(user.getId(), EntityStatus.INACTIVE.name(), LocalDate.now());

        reviewRepository.updateAllReviewsByUserId(user.getId(), EntityStatus.INACTIVE.name(), LocalDate.now());

        user.getRoles().forEach(userHasRoleService::updateUserHasRoleToInactive);

        user.getJobs().forEach(savedJobService::updateUserSavedJobToInactive);

        user.setState(EntityStatus.INACTIVE);
        user.setDeactivatedAt(LocalDate.now());
    }

    @Transactional
    public void deleteUsers(Set<Long> ids){
        Set<User> userSet = userRepository.findAllByIdIn(ids);
        if(userSet.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userSet.forEach(this::deactivateUser);

        userRepository.saveAll(userSet);
    }

    @Transactional
    public UserResponse restoreUser(long id) {
        if(userHistoryRepository.existsById(id)){
            throw new AppException(ErrorCode.USER_ARCHIVED_IN_HISTORY);
        }
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getState() == EntityStatus.INACTIVE) {
            resumeRepository.updateAllResumesByUserId(user.getId(), EntityStatus.ACTIVE.name(), null);

            reviewRepository.updateAllReviewsByUserId(user.getId(), EntityStatus.ACTIVE.name(), null);

            user.getRoles().forEach(userHasRoleService::updateUserHasRoleToActive);

            user.getJobs().forEach(savedJobService::updateUserSavedJobToActive);

            user.setState(EntityStatus.ACTIVE);
            user.setDeactivatedAt(null);
            return userMapper.toUserResponse(userRepository.save(user));
        }else {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }
    }

    public PageResponse<JobResponse> getAllAppliedJobsByUser(int pageNo, int pageSize, String sortBy, long userId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Resume> resumes = resumeRepository.findAllByUserId(user.getId());

        Page<Job> jobPage = jobRepository.findAllByResumesIn(resumes, pageable);

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(jobMapper.toJobResponseList(jobPage.getContent()))
                .build();
    }

    public PageResponse<ResumeResponse> getAllResumesByUser(int pageNo, int pageSize, String sortBy, long userId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<Resume> resumePage = resumeRepository.findAllByUser(user, pageable);

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)  // Thêm 1 để bắt đầu từ trang 1
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(resumeMapper.toResumeResponseList(resumePage.getContent()))
                .build();
    }

    private User findActiveUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }


}
