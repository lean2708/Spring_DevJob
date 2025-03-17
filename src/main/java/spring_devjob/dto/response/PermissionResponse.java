package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionResponse extends BaseResponse {
    String name;

    String module;
    String apiPath;
    String method;

}
