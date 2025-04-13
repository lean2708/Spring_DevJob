package spring_devjob.service;

import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.RoleResponse;

import java.util.Set;

public interface RoleService {

    RoleResponse create(RoleRequest request);

    RoleResponse fetchRoleById(long id);

    PageResponse<RoleResponse> fetchAllRoles(int pageNo, int pageSize, String sortBy);

    RoleResponse update(long id, RoleRequest request);

    void delete(long id);

    void deleteRoles(Set<Long> ids);
}
