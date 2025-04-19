package spring_devjob.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.RoleHasPermission;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.relationship.RoleHasPermissionRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.service.PageableService;
import spring_devjob.service.RoleService;
import spring_devjob.service.relationship.UserHasRoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static spring_devjob.constants.RoleEnum.IMMUTABLE_SYSTEM_ROLES;

@Slf4j(topic = "ROLE-SERVICE")
@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PageableService pageableService;
    private final PermissionRepository permissionRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;
    private final UserHasRoleService userHasRoleService;

    @Override
    public RoleResponse create(RoleRequest request){
        if(roleRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Role role = roleMapper.toRole(request);
        roleRepository.save(role);

        if(!CollectionUtils.isEmpty(request.getPermissions())){
            Set<Permission> permissionSet = permissionRepository.findAllByNameIn(request.getPermissions());

            Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                    .map(permission -> new RoleHasPermission(role,permission))
                    .collect(Collectors.toSet());

            role.setPermissions(new HashSet<>(roleHasPermissionRepository.saveAll(roleHasPermissions)));
        }

        return roleMapper.toRoleResponse(role);
    }

    @Override
    public RoleResponse fetchRoleById(long id){
        Role roleDB = findRoleById(id);

        return roleMapper.toRoleResponse(roleDB);
    }


    @Override
    public PageResponse<RoleResponse> fetchAllRoles(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy, Role.class);

        Page<Role> rolePage = roleRepository.findAll(pageable);

        return PageResponse.<RoleResponse>builder()
                .page(rolePage.getNumber() + 1)
                .size(rolePage.getSize())
                .totalPages(rolePage.getTotalPages())
                .totalItems(rolePage.getTotalElements())
                .items(roleMapper.toRoleResponseList(rolePage.getContent()))
                .build();
    }

    @Transactional
    @Override
    public RoleResponse update(long id, RoleRequest request){
        Role roleDB = findRoleById(id);

        if (IMMUTABLE_SYSTEM_ROLES.contains(roleDB.getName())) {
            throw new AppException(ErrorCode.CANNOT_CHANGE_DEFAULT_ROLE);
        }

        roleMapper.updateRole(roleDB, request);

        if(!CollectionUtils.isEmpty(request.getPermissions())){
            Set<Permission> permissionSet = permissionRepository.findAllByNameIn(request.getPermissions());

            roleHasPermissionRepository.deleteByRole(roleDB);

            Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                    .map(permission -> new RoleHasPermission(roleDB,permission))
                    .collect(Collectors.toSet());

            roleDB.setPermissions(new HashSet<>(roleHasPermissionRepository.saveAll(roleHasPermissions)));
        }

        return roleMapper.toRoleResponse(roleRepository.save(roleDB));
    }

    @Transactional
    @Override
    public void delete(long id){
        Role roleDB = findRoleById(id);

        deactivateRole(roleDB);

        roleRepository.delete(roleDB);
    }

    private void deactivateRole(Role role){
        if (IMMUTABLE_SYSTEM_ROLES.contains(role.getName())) {
            throw new AppException(ErrorCode.CANNOT_CHANGE_DEFAULT_ROLE);
        }

        userHasRoleService.deleteUserHasRoleByRole(role.getId());

        deleteRoleHasPermissionByRole(role.getId());
    }

    private void deleteRoleHasPermissionByRole(long roleId){
        List<RoleHasPermission> roleHasPermissions = roleHasPermissionRepository.findByRoleId(roleId);
        roleHasPermissionRepository.deleteAll(roleHasPermissions);
    }

    @Transactional
    @Override
    public void deleteRoles(Set<Long> ids) {
        Set<Role> roleSet = roleRepository.findAllByIdIn(ids);

        if(roleSet.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleSet.forEach(this::deactivateRole);

        roleRepository.deleteAllInBatch(roleSet);
    }

    private Role findRoleById(long id) {
        return roleRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
    }

}

