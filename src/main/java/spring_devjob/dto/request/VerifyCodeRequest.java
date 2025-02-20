package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyCodeRequest {
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email phải có định dạng hợp lệ")
    String email;
    @NotBlank(message = "VerificationCode không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã xác nhận phải có đúng 6 chữ số")
    String verificationCode;
}
