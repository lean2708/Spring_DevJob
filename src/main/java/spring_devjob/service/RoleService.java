package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.RoleHasPermission;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleHasPermissionRepository;
import spring_devjob.repository.RoleRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PageableService pageableService;
    private final PermissionRepository permissionRepository;
    private final RoleHasPermissionRepository roleHasPermissionRepository;
    private final UserHasRoleService userHasRoleService;

    public RoleResponse create(RoleRequest request){
        if(roleRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        Role role = roleMapper.toRole(request);

        if(!CollectionUtils.isEmpty(request.getPermissions())){
            Set<Permission> permissionSet = permissionRepository.findAllByNameIn(request.getPermissions());

            Set<RoleHasPermission> roleHasPermissions = permissionSet.stream()
                    .map(permission -> new RoleHasPermission(role,permission))
                    .collect(Collectors.toSet());

            role.setPermissions(new HashSet<>(roleHasPermissionRepository.saveAll(roleHasPermissions)));
        }

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }

    public RoleResponse fetchRoleById(long id){
        Role roleDB = findActiveRoleById(id);

        return roleMapper.toRoleResponse(roleDB);
    }


    public PageResponse<RoleResponse> fetchAllRoles(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Role> rolePage = roleRepository.findAll(pageable);

        return PageResponse.<RoleResponse>builder()
                .page(rolePage.getNumber() + 1)
                .size(rolePage.getSize())
                .totalPages(rolePage.getTotalPages())
                .totalItems(rolePage.getTotalElements())
                .items(roleMapper.toRoleResponseList(rolePage.getContent()))
                .build();
    }

    public RoleResponse update(long id, RoleRequest request){
        Role roleDB = findActiveRoleById(id);

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
    public void delete(long id){
        Role roleDB = findActiveRoleById(id);

        deactivateRole(roleDB);

        roleRepository.save(roleDB);
    }

    private void deactivateRole(Role role){
        role.getUsers().forEach(userHasRoleService::updateUserHasRoleToInactive);

        role.getPermissions().forEach(this::updateRoleHasPermissionToInactive);

        role.setState(EntityStatus.INACTIVE);
        role.setDeactivatedAt(LocalDate.now());
    }

    private void updateRoleHasPermissionToInactive(RoleHasPermission roleHasPermission){
        roleHasPermission.setState(EntityStatus.INACTIVE);
        roleHasPermissionRepository.save(roleHasPermission);
    }

    @Transactional
    public void deleteRoles(Set<Long> ids) {
        Set<Role> roleSet = roleRepository.findAllByIdIn(ids);

        if(roleSet.isEmpty()){
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleSet.forEach(this::deactivateRole);

        roleRepository.saveAll(roleSet);
    }

    private Role findActiveRoleById(long id) {
        return roleRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
    }

}

