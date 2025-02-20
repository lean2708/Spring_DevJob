package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.ResumeStateEnum;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.basic.UserBasic;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeResponse extends BaseResponse {
    String cvUrl;

    ResumeStateEnum status;
    boolean primaryCv;

    UserBasic user;
    JobBasic job;
}
