package spring_devjob.controller;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.service.UserService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class UserController {
    private final UserService userService;


    @Operation(summary = "Create User with Role",
            description = "API này được sử dụng để tạo user và gán role vào user đó")
    @PostMapping("/user")
    public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request){
         return ApiResponse.<UserResponse>builder()
                 .code(HttpStatus.CREATED.value())
                 .message("Create User With Role")
                 .result(userService.create(request))
                 .build();
     }

    @GetMapping("/user/{id}")
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

    @PutMapping("/user/{id}")
    public ApiResponse<UserResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                @PathVariable long id, @RequestBody UserRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update User By Id")
                .result(userService.update(id, request))
                .build();
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        userService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete User By Id")
                .result(null)
                .build();
    }
}
