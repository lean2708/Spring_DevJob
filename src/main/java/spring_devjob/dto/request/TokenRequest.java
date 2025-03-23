package spring_devjob.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenRequest {
    @NotBlank(message = "accessToken không được để trống")
    String accessToken;
}
