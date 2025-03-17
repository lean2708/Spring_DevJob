package spring_devjob.controller;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.JobService;
import spring_devjob.service.ResumeService;
import spring_devjob.service.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class UserController {
    private final UserService userService;
    private final JobService jobService;
    private final ResumeService resumeService;

    @Operation(summary = "Create User with Role",
            description = "API này được sử dụng để tạo user và gán role vào user đó")
    @PostMapping("/users")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request){
         return ApiResponse.<UserResponse>builder()
                 .code(HttpStatus.CREATED.value())
                 .message("Create User With Role")
                 .result(userService.create(request))
                 .build();
     }

    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> fetchUser(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                   @PathVariable long id){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch User By Id")
                .result(userService.fetchUserById(id))
                .build();
    }

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

    @PutMapping("/users/{id}")
    public ApiResponse<UserResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                @PathVariable long id, @RequestBody UserRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update User By Id")
                .result(userService.update(id, request))
                .build();
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        userService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete User By Id")
                .result(null)
                .build();
    }

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

    @GetMapping("/users/{userId}/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> fetchAllByUser(@RequestParam(defaultValue = "1") int pageNo,
                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                    @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                    @RequestParam(required = false) String sortBy,
                                                                    @Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable Long userId){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(resumeService.getAllResumesByUser(pageNo, pageSize, sortBy, userId))
                .message("Fetch All Resumes By User With Pagination")
                .build();
    }

    @Operation(summary = "Get list of applied jobs",
            description = "API này để láy danh sách job mà người dùng đã ứng tuyển")
    @GetMapping("/users/{userId}/applied-jobs")
    public ApiResponse<PageResponse<JobResponse>> getAllAppliedJobsByUser(@RequestParam(defaultValue = "1") int pageNo,
                                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                                          @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                          @RequestParam(required = false) String sortBy,
                                                                          @Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable Long userId){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.getAllAppliedJobsByUser(pageNo, pageSize, sortBy, userId))
                .message("Fetch All Jobs By User")
                .build();
    }
}
