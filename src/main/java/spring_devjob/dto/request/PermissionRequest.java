package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRequest {
    @NotBlank(message = "Name không được để trống")
    String name;

    @NotBlank(message = "Module không được để trống")
    String module;

    @NotBlank(message = "ApiPath không được để trống")
    String apiPath;

    @NotBlank(message = "Method không được để trống")
    String method;

}
