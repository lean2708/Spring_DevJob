package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.entity.Permission;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.PermissionMapper;
import spring_devjob.repository.PermissionRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final PageableService pageableService;


    public PermissionResponse fetchPermissionById(long id){
        Permission permissionDB = permissionRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.PERMISSION_NOT_EXISTED));

        return permissionMapper.toPermissionResponse(permissionDB);
    }

    public PageResponse<PermissionResponse> fetchAllPermissions(int pageNo, int pageSize,  String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Permission> permissionPage = permissionRepository.findAll(pageable);

        return PageResponse.<PermissionResponse>builder()
                .page(permissionPage.getNumber() + 1)
                .size(permissionPage.getSize())
                .totalPages(permissionPage.getTotalPages())
                .totalItems(permissionPage.getTotalElements())
                .items(permissionMapper.toPermissionResponseList(permissionPage.getContent()))
                .build();
    }

}
