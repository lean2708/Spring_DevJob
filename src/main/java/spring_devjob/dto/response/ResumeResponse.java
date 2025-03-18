package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.basic.UserBasic;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeResponse extends BaseResponse {
    String name;

    String cvUrl;

    boolean primaryCv;

    UserBasic user;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Set<JobBasic> jobs;
}
