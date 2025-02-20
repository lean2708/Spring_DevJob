package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntrospectResponse {
    boolean valid;
}
