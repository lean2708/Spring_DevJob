package spring_devjob.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.GenderEnum;
import spring_devjob.dto.validator.EnumPattern;

import java.util.List;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    @NotBlank(message = "Name không được để trống")
    String name;
    @NotBlank(message = "Email không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email phải có định dạng hợp lệ")
    String email;
    @Size(min = 5, message = "Password phải từ 5 kí tự trở lên")
    @NotBlank(message = "Password không được để trống")
    String password;
    String avatarUrl;
    int age;
    @EnumPattern(name = "gender", regexp = "FEMALE|MALE|OTHER")
    GenderEnum gender;
    String address;

    Long companyId;
    List<Long> roleId;
}
