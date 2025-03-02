package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.ResumeStateEnum;
import spring_devjob.dto.validator.EnumPattern;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeRequest {
    @NotBlank(message = "Name không được để trống")
    String name;
    String cvUrl;

    @EnumPattern(name = "status", regexp = "PENDING|REVIEWING|APPROVED|REJECTED")
    ResumeStateEnum status;

    boolean primaryCv;

    @NotNull(message = "Job không được để trống")
    Long jobId;

}
