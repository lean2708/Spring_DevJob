package spring_devjob.controller;
import io.swagger.v3.oas.annotations.Operation;
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
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.UserCreationRequest;
import spring_devjob.dto.request.UserUpdateRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.RestoreService;
import spring_devjob.service.UserService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class UserController {
    private final UserService userService;
    private final RestoreService restoreService;

    @Operation(summary = "Create User with Role",
            description = "API này được sử dụng để tạo user và gán role vào user đó")
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/users")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserCreationRequest request){
         return ApiResponse.<UserResponse>builder()
                 .code(HttpStatus.CREATED.value())
                 .message("Create User With Role")
                 .result(userService.create(request))
                 .build();
     }

    @PreAuthorize("hasAuthority('FETCH_USER_BY_ID')")
    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> fetchUser(@Positive(message = "ID phải lớn hơn 0")
                                                   @PathVariable long id){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch User By Id")
                .result(userService.fetchUserById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_USERS')")
    @GetMapping("/users")
    public ApiResponse<PageResponse<UserResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(userService.fetchAllUsers(pageNo, pageSize, sortBy))
                .message("Fetch All Users With Pagination")
                .build();
    }

    @Operation(summary = "Update User (No update Password)",
              description = "API này được sử dụng để update user (Gán company nếu người dùng là HR)")
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/users/{id}")
    public ApiResponse<UserResponse> update(@Positive(message = "ID phải lớn hơn 0")
                                                @PathVariable long id, @RequestBody UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update User By Id")
                .result(userService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> delete(@Positive(message = "ID phải lớn hơn 0")
                                        @PathVariable long id){
        userService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete User By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_MULTIPLE_USERS')")
    @DeleteMapping("/users")
    public ApiResponse<Void> deleteUsers(@RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                         Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        userService.deleteUsers(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Users")
                .result(null)
                .build();
    }

    @Operation(summary = "Restore User",
            description = "API này được sử dụng để phục hồi user đã bị xóa mềm")
    @PreAuthorize("hasAuthority('RESTORE_USER')")
    @PatchMapping("/users/{id}/restore")
    public ApiResponse<UserResponse> restore(@Positive(message = "ID phải lớn hơn 0")
                                             @PathVariable long id) {
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Restore User By Id")
                .result(restoreService.restoreUser(id, EntityStatus.INACTIVE))
                .build();
    }

    @Operation(summary = "Fetch All Resumes By User With Pagination",
            description = "API này được sử dụng để lấy tất cả CV của User")
    @PreAuthorize("hasAuthority('FETCH_RESUMES_BY_USER')")
    @GetMapping("/users/{userId}/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> fetchAllByUser(@RequestParam(defaultValue = "1") int pageNo,
                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                    @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                    @RequestParam(required = false) String sortBy,
                                                                    @Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable Long userId){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(userService.getAllResumesByUser(pageNo, pageSize, sortBy, userId))
                .message("Fetch All Resumes By User With Pagination")
                .build();
    }

    @Operation(summary = "Get list of applied jobs",
            description = "API này để láy danh sách job mà người dùng đã ứng tuyển")
    @PreAuthorize("hasAuthority('FETCH_APPLIED_JOBS')")
    @GetMapping("/users/{userId}/applied-jobs")
    public ApiResponse<PageResponse<JobResponse>> getAllAppliedJobsByUser(@RequestParam(defaultValue = "1") int pageNo,
                                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                                          @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                          @RequestParam(required = false) String sortBy,
                                                                          @Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable Long userId){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(userService.getAllAppliedJobsByUser(pageNo, pageSize, sortBy, userId))
                .message("Fetch All Jobs By User")
                .build();
    }
}
