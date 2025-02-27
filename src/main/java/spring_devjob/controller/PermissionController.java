package spring_devjob.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.PermissionRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.PermissionResponse;
import spring_devjob.service.PermissionService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping("/permissions")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Permission")
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping("/permissions/{id}")
    public ApiResponse<PermissionResponse> fetchRoleById(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                             @PathVariable long id){
        return ApiResponse.<PermissionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Permission By Id")
                .result(permissionService.fetchPermissionById(id))
                .build();
    }

    @GetMapping("/permissions")
    public ApiResponse<PageResponse<PermissionResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                      @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                                  @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                      @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<PermissionResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(permissionService.fetchAllPermissions(pageNo, pageSize, sortBy))
                .message("Fetch All Permissions With Pagination")
                .build();
    }

    @PutMapping("/permissions/{id}")
    public ApiResponse<PermissionResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                      @PathVariable long id, @RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Permission By Id")
                .result(permissionService.update(id, request))
                .build();
    }

    @DeleteMapping("/permissions/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        permissionService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Permission By Id")
                .result(null)
                .build();
    }

    @DeleteMapping("/permissions")
    public ApiResponse<Void> deletePermissions(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                                   List<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        permissionService.deletePermissions(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Permissions")
                .result(null)
                .build();
    }
}
