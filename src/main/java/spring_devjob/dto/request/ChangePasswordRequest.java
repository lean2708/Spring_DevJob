package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Size(min = 6, message = "Password cũ phải từ 6 kí tự trở lên")
    String oldPassword;
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Password mới phải từ 6 kí tự trở lên")
    String newPassword;
    @Size(min = 6, message = "confirmPassword phải từ 6 kí tự trở lên")
    @NotBlank(message = "confirmPassword không được để trống")
    String confirmPassword;
}
