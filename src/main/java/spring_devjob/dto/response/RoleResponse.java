package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse extends BaseResponse {
    String description;

    List<PermissionResponse> permissions;
}
