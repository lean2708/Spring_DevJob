package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.ResumeStateEnum;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.basic.UserBasic;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeResponse extends BaseResponse {
    String name;

    String cvUrl;

    ResumeStateEnum resumeStatus;
    boolean primaryCv;

    UserBasic user;
    EntityBasic job;
}
