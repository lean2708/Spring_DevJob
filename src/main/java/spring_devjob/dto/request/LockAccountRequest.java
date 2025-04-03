package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LockAccountRequest {

    @Size(min = 5, message = "Password phải từ 5 kí tự trở lên")
    @NotBlank(message = "Password không được để trống")
    String password;
}
