package spring_devjob.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.*;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.IntrospectResponse;
import spring_devjob.dto.response.LoginResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.VerificationCodeEntity;
import spring_devjob.service.AuthService;
import spring_devjob.service.ForgotPasswordService;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> authenticate(@Valid @RequestBody LoginRequest request) throws JOSEException {
        return ApiResponse.<LoginResponse>builder()
                .code(HttpStatus.OK.value())
                .result(authService.login(request))
                .message("Login")
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.CREATED.value())
                .result(authService.register(request))
                .message("Register")
                .build();
    }

    @GetMapping("/myInfo")
    public ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .code(HttpStatus.OK.value())
                .result(authService.getMyInfo())
                .message("My Info")
                .build();
    }

    @PostMapping("/introspect")
    private ApiResponse<IntrospectResponse> introspected(@Valid @RequestBody TokenRequest request) throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .code(HttpStatus.OK.value())
                .result(authService.introspect(request))
                .message("Introspect")
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody TokenRequest request) throws JOSEException, ParseException {
        authService.logout(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Logout")
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<VerificationCodeEntity> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.<VerificationCodeEntity>builder()
                .code(HttpStatus.OK.value())
                .result(forgotPasswordService.forgotPassword(request))
                .message("Mã xác nhận đã được gửi vào email của bạn")
                .build();
    }

    @PostMapping("/verify-code")
    public ApiResponse<Void> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        boolean isValid = forgotPasswordService.verifyCode(request.getEmail(), request.getVerificationCode());
        if (isValid) {
            return ApiResponse.<Void>builder()
                    .code(HttpStatus.OK.value())
                    .message("Mã xác nhận hợp lệ")
                    .build();
        } else {
            return ApiResponse.<Void>builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message("Mã xác nhận không hợp lệ")
                    .build();
        }
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        forgotPasswordService.changePassword(request);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Mật khẩu đã được thay đổi thành công")
                .build();
    }
}
