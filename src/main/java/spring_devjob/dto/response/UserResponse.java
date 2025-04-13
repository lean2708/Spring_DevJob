package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.GenderEnum;
import spring_devjob.dto.basic.EntityBasic;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse extends BaseResponse{
    String name;
    String email;
    String phone;
    String avatarUrl;
    int age;
    @Enumerated(EnumType.STRING)
    GenderEnum gender;
    String address;

    String createdBy;
    String updatedBy;
    LocalDate createdAt;
    LocalDate updatedAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    EntityBasic company;

    Set<EntityBasic> roles;
}
