package spring_devjob.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.constants.ApplicationStatusEnum;
import spring_devjob.dto.validator.EnumPattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCVStatusRequest {
    @NotNull(message = "ResumeStatus không được để trống")
    @EnumPattern(name = "ResumeStatus", regexp = "PENDING|REVIEWING|APPROVED|REJECTED")
    ApplicationStatusEnum resumeStatus;
}
