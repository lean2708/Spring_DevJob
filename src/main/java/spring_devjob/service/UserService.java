package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import spring_devjob.repository.relationship.UserHasRoleRepository;
import spring_devjob.service.relationship.UserHasRoleService;

import java.util.*;
import java.util.stream.Collectors;

import static redis.clients.jedis.resps.StreamConsumerInfo.INACTIVE;
import static spring_devjob.constants.EntityStatus.LOCKED;


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
    private final PageableService pageableService;
    private final UserHasRoleRepository userHasRoleRepository;
    private final UserHasRoleService userHasRoleService;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final EntityDeactivationService entityDeactivationService;


    public UserResponse create(UserCreationRequest request){
        checkUserExistenceAndStatus(request.getEmail(), request.getPhone());

        User user = userMapper.userCreationToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        // role user mac dinh
        Set<UserHasRole> roles = new HashSet<>();
        roles.add(userHasRoleService.saveUserHasRole(user, RoleEnum.USER));
        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            Set<Role> roleSet = roleRepository.findAllByIdIn(request.getRoleIds());

            Set<UserHasRole> userRoles = roleSet.stream()
                    .filter(role -> !role.getName().equals(RoleEnum.USER.toString()))
                    .map(role -> new UserHasRole(user, role))
                    .collect(Collectors.toSet());

            roles.addAll(userHasRoleRepository.saveAll(userRoles));
        }
        user.setRoles(roles);

       return userMapper.toUserResponse(user);
    }

    public void checkUserExistenceAndStatus(String email, String phone){
        if(userRepository.countByEmailAndState(email, EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        if(userRepository.countByEmailAndState(email, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_DISABLED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.PHONE_DISABLED);
        }
        if(userRepository.countByEmailAndState(email, EntityStatus.LOCKED.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_LOCKED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.LOCKED.name()) > 0){
            throw new AppException(ErrorCode.PHONE_LOCKED);
        }
    }

    public UserResponse fetchUserById(Long id){
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

        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            userHasRoleRepository.deleteByUser(userDB);

            Set<UserHasRole> roles = new HashSet<>();
            // role user mac dinh
            roles.add(userHasRoleService.saveUserHasRole(userDB, RoleEnum.USER));

            Set<Role> roleSet = roleRepository.findAllByIdIn(request.getRoleIds());

            Set<UserHasRole> userRoles = roleSet.stream()
                    .filter(role -> !role.getName().equals(RoleEnum.USER.toString()))
                    .map(role -> new UserHasRole(userDB, role))
                    .collect(Collectors.toSet());

            roles.addAll(userHasRoleRepository.saveAll(userRoles));

            userDB.setRoles(roles);
        }
        Set<String> roleNames = userDB.getRoles().stream()
                .map(role -> role.getRole().getName())
                .collect(Collectors.toSet());
        boolean isHR = roleNames.contains(RoleEnum.HR.name());

        if(request.getCompanyId() != null && isHR){
            companyRepository.findById(request.getCompanyId()).ifPresent(userDB::setCompany);
        }

        return userMapper.toUserResponse(userRepository.save(userDB));
    }


    @Transactional
    public void delete(long id){
        User userDB = findActiveUserById(id);

        entityDeactivationService.deactivateUser(userDB, EntityStatus.INACTIVE);
    }


    @Transactional
    public void deleteUsers(Set<Long> ids){
        Set<User> userSet = userRepository.findAllByIdIn(ids);
        if(userSet.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userSet.forEach(user -> entityDeactivationService.deactivateUser(user, EntityStatus.INACTIVE));
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
