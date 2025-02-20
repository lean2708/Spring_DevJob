package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CompanyMapper companyMapper;
    private final RoleMapper roleMapper;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final ResumeRepository resumeRepository;
    private final PageableService pageableService;


    public UserResponse create(UserRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(request.getCompany() != null && !request.getCompany().isEmpty()){
            Company company = companyRepository.findByName(request.getCompany());
            user.setCompany(company);
        }

        if(request.getRole() != null && !request.getRole().isEmpty()){
            List<Role> roles = roleRepository.findAllByNameIn(request.getRole());
            user.setRoles(roles);
        }

       return convertUserResponse(userRepository.save(user));
    }

    public UserResponse fetchUserById(long id){
        User userDB = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return convertUserResponse(userDB);
    }

    @PreAuthorize("hasRole('ADMIN')")
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

        if(request.getCompany() != null && !request.getCompany().isEmpty()){
            Company company = companyRepository.findByName(request.getCompany());
            userDB.setCompany(company);
        }

        if(request.getRole() != null && !request.getRole().isEmpty()){
            List<Role> roles = roleRepository.findAllByNameIn(request.getRole());
            userDB.setRoles(roles);
        }

        return convertUserResponse(userRepository.save(userDB));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(long id){
        User userDB = userRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userDB.getCompany().getUsers().remove(userDB);
        companyRepository.save(userDB.getCompany());

        resumeRepository.deleteAll(userDB.getResumes());

        userRepository.delete(userDB);
    }

    public List<UserResponse> convertListUserResponse(List<User> userList){
        List<UserResponse> userResponseList = new ArrayList<>();
        for(User user : userList){
            UserResponse response = convertUserResponse(user);
            userResponseList.add(response);
        }
        return userResponseList;
    }

    public UserResponse convertUserResponse(User user){
        UserResponse response = userMapper.toUserResponse(user);

        CompanyBasic companyBasic = (user.getCompany() != null) ?
                companyMapper.toCompanyBasic(user.getCompany()) : null;
        response.setCompany(companyBasic);

        List<RoleBasic> roleBasics = (user.getRoles() != null) ?
                roleMapper.toRoleBasics(user.getRoles()) : null;
        response.setRoles(roleBasics);

        return response;
    }
}
