package spring_devjob.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/permissions/{id}")
    public ApiResponse<PermissionResponse> fetchRoleById(@Positive(message = "ID phải lớn hơn 0")
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

}
