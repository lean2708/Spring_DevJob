package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spring_devjob.constants.RoleEnum;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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


    public UserResponse create(UserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(request.getCompanyId() != null){
            companyRepository.findById(request.getCompanyId()).ifPresent(user::setCompany);
        }

        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            user.setRoles(roleRepository.findAllByIdIn(request.getRoleIds()));
        }else{
            Role userRole = roleRepository.findByName(RoleEnum.USER.name()).orElseThrow(
                    () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
            user.setRoles(Set.of(userRole));
        }

       return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse fetchUserById(long id){
        User userDB = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(userDB);
    }

    public PageResponse<UserResponse> fetchAllUsers(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> responses =  convertListUserResponse(userPage.getContent());

        return PageResponse.<UserResponse>builder()
                .page(userPage.getNumber() + 1)
                .size(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalItems(userPage.getTotalElements())
                .items(responses)
                .build();
    }

    public UserResponse update(long id, UserRequest request){
        User userDB = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(userDB, request);

        userDB.setPassword(passwordEncoder.encode(userDB.getPassword()));

        if(request.getCompanyId() != null){
            companyRepository.findById(request.getCompanyId()).ifPresent(userDB::setCompany);
        }

        if(!CollectionUtils.isEmpty(request.getRoleIds())){
            userDB.setRoles(roleRepository.findAllByIdIn(request.getRoleIds()));
        }

        return userMapper.toUserResponse(userRepository.save(userDB));
    }

    @Transactional
    public void delete(long id){
        User userDB = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        processUserDeletion(userDB);

        userRepository.delete(userDB);
    }

    private void processUserDeletion(User user) {
        if(!CollectionUtils.isEmpty(user.getResumes())){
            resumeRepository.deleteAll(user.getResumes());
        }

        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().removeAll(user.getRoles());
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteUsers(Set<Long> ids){
        Set<User> userSet = userRepository.findAllByIdIn(ids);
        if(userSet.isEmpty()){
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        userSet.forEach(this::processUserDeletion);

        userRepository.deleteAllInBatch(userSet);
    }

    public List<UserResponse> convertListUserResponse(List<User> userList){
        List<UserResponse> userResponseList = new ArrayList<>();
        for(User user : userList){
            UserResponse response = userMapper.toUserResponse(user);
            userResponseList.add(response);
        }
        return userResponseList;
    }

}
