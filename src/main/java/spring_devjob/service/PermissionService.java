package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring_devjob.dto.request.PermissionRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.entity.Permission;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.PermissionMapper;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final RoleRepository roleRepository;
    private final PageableService pageableService;

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse create(PermissionRequest request){
        if(permissionRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.PERMISSION_EXISTED);
        }

        Permission permission = permissionMapper.toPermission(request);

        return permissionMapper.toPermissionResponse(permissionRepository.save(permission));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse fetchPermissionById(long id){
        Permission permissionDB = permissionRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        return permissionMapper.toPermissionResponse(permissionDB);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<PermissionResponse> fetchAllPermissions(int pageNo, int pageSize,  String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Permission> permissionPage = permissionRepository.findAll(pageable);

        List<PermissionResponse> permissionResponses = new ArrayList<>();
        for(Permission permission : permissionPage.getContent()){
            PermissionResponse response = permissionMapper.toPermissionResponse(permission);
            permissionResponses.add(response);
        }

        return PageResponse.<PermissionResponse>builder()
                .page(permissionPage.getNumber() + 1)
                .size(permissionPage.getSize())
                .totalPages(permissionPage.getTotalPages())
                .totalItems(permissionPage.getTotalElements())
                .items(permissionResponses)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse update(long id, PermissionRequest request){
        Permission permissionDB = permissionRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        permissionMapper.updatePermission(permissionDB, request);

        return permissionMapper.toPermissionResponse(permissionRepository.save(permissionDB));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(long id){
        Permission permissionDB = permissionRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        permissionDB.getRoles().forEach(role -> {
            role.getPermissions().remove(permissionDB);
            roleRepository.save(role);
        });

        permissionRepository.delete(permissionDB);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePermissions(List<Long> ids){
        List<Permission> permissionList = permissionRepository.findAllByIdIn(ids);
        if(permissionList.isEmpty()){
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        for(Permission permission : permissionList){
            permission.getRoles().forEach(role -> {
                role.getPermissions().remove(permission);
                roleRepository.save(role);
            });
        }

        permissionRepository.deleteAllInBatch(permissionList);
    }
}
