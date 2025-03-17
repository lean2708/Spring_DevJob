package spring_devjob.service;

import jakarta.transaction.Transactional;
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
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.*;

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


    public UserResponse create(UserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        if(userRepository.existsByPhone(request.getPhone())){
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        User user = userMapper.toUser(request);

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
            userHasRoleService.saveUserHasRole(user, RoleEnum.USER);
        }

       return userMapper.toUserResponse(user);
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

    public UserResponse update(long id, UserRequest request){
        User userDB = findActiveUserById(id);

        userMapper.updateUser(userDB, request);

        if (!StringUtils.isBlank(request.getPassword())) {
            userDB.setPassword(passwordEncoder.encode(request.getPassword()));
        }

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
        resumeRepository.updateAllResumesToInactiveByUserId(user.getId(), EntityStatus.INACTIVE, LocalDate.now());

        reviewRepository.updateAllReviewsToInactiveByUserId(user.getId(), EntityStatus.INACTIVE, LocalDate.now());

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

    private User findActiveUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
