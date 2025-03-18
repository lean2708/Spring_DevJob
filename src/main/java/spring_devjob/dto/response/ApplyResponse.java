package spring_devjob.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.ApplicationStatusEnum;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ApplyResponse {
    Long jobId;
    Long resumeId;
    Long userId;
    ApplicationStatusEnum applicationStatus;
    LocalDate appliedAt;
}
