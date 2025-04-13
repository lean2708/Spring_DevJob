package spring_devjob.service;

import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.PermissionResponse;

public interface PermissionService {

    PermissionResponse fetchPermissionById(long id);

    PageResponse<PermissionResponse> fetchAllPermissions(int pageNo, int pageSize, String sortBy);
}
