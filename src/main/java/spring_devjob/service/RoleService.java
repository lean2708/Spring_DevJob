package spring_devjob.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;
import spring_devjob.entity.Subscriber;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PageableService pageableService;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleResponse create(RoleRequest request){
        if(roleRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Role role = roleMapper.toRole(request);

        if(request.getPermissions() != null && !request.getPermissions().isEmpty()){
            List<Permission> permissions = permissionRepository.findAllByNameIn(request.getPermissions());
            role.setPermissions(permissions);
        }

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    public RoleResponse fetchRoleById(long id){
        Role roleDB = roleRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        return roleMapper.toRoleResponse(roleDB);
    }


    public PageResponse<RoleResponse> fetchAllRoles(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Role> rolePage = roleRepository.findAll(pageable);

        List<RoleResponse> responses =  new ArrayList<>();
        for(Role role : rolePage.getContent()){
            responses.add(roleMapper.toRoleResponse(role));
        }

        return PageResponse.<RoleResponse>builder()
                .page(rolePage.getNumber() + 1)
                .size(rolePage.getSize())
                .totalPages(rolePage.getTotalPages())
                .totalItems(rolePage.getTotalElements())
                .items(responses)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse update(long id, RoleRequest request){
        Role roleDB = roleRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        roleMapper.updateRole(roleDB, request);

        if(request.getPermissions() != null && !request.getPermissions().isEmpty()){
            List<Permission> permissions = permissionRepository.findAllByNameIn(request.getPermissions());
            roleDB.setPermissions(permissions);
        }

        return roleMapper.toRoleResponse(roleRepository.save(roleDB));
    }

    public void delete(long id){
        Role roleDB = roleRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        roleDB.getUsers().forEach(user -> {
            user.getRoles().remove(roleDB);
            userRepository.save(user);
        });

        roleRepository.delete(roleDB);
    }

    public void deleteRoles(List<Long> ids) {
        List<Role> roleList = roleRepository.findAllByIdIn(ids);

        if(roleList.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        for(Role role : roleList){
            role.getUsers().forEach(user -> {
                user.getRoles().remove(role);
                userRepository.save(user);
            });
        }

        roleRepository.deleteAllInBatch(roleList);
    }
}

