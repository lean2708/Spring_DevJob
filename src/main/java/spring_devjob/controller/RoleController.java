package spring_devjob.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.service.RoleService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('CREATE_ROLE')")
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Role")
                .result(roleService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ROLE_BY_ID')")
    @GetMapping("/roles/{id}")
    public ApiResponse<RoleResponse> fetchRoleById(@Positive(message = "ID phải lớn hơn 0")
                                                       @PathVariable long id){
        return ApiResponse.<RoleResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Role By Id")
                .result(roleService.fetchRoleById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_ROLES')")
    @GetMapping("/roles")
    public ApiResponse<PageResponse<RoleResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                @RequestParam(defaultValue = "1") int pageNo,
                                                             @RequestParam(defaultValue = "10") int pageSize,
                                                            @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<RoleResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(roleService.fetchAllRoles(pageNo, pageSize, sortBy))
                .message("Fetch All Roles With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_ROLE')")
    @PutMapping("/roles/{id}")
    public ApiResponse<RoleResponse> update(@Positive(message = "ID phải lớn hơn 0")
                                            @PathVariable long id, @RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Role By Id")
                .result(roleService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_ROLE')")
    @DeleteMapping("/roles/{id}")
    public ApiResponse<Void> delete(@Positive(message = "ID phải lớn hơn 0")
                                    @PathVariable long id){
        roleService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Role By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_MULTIPLE_ROLES')")
    @DeleteMapping("/roles")
    public ApiResponse<Void> deleteRoles(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                         Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        roleService.deleteRoles(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Roles")
                .result(null)
                .build();
    }
}
