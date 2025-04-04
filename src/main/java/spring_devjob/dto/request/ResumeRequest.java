package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeRequest {
    @NotBlank(message = "Name không được để trống")
    String name;
    String cvUrl;

    Boolean primaryCv;

}
