package spring_devjob.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {
    @NotBlank(message = "forgotPasswordToken không được để trống")
    String forgotPasswordToken;
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Password mới phải từ 6 kí tự trở lên")
    String newPassword;
    @Size(min = 6, message = "confirmPassword phải từ 6 kí tự trở lên")
    @NotBlank(message = "confirmPassword không được để trống")
    String confirmPassword;
}
