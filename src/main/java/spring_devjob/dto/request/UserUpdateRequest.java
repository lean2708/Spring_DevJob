package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.GenderEnum;
import spring_devjob.dto.validator.EnumPattern;

import java.util.Set;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String name;
    String avatarUrl;
    Integer age;

    GenderEnum gender;
    String address;

    Long companyId;
    Set<Long> roleIds;
}
