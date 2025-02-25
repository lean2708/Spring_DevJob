package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String accessToken;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String refreshToken;
    boolean authenticated;
    String email; 
    List<String> role;
}
