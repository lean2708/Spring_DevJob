package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.LevelEnum;
import spring_devjob.dto.basic.EntityBasic;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResponse extends BaseResponse {
    String location;
    double salary;
    int quantity;
    LevelEnum level;

    String description;

    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate startDate;
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate endDate;
    boolean jobStatus;

    EntityBasic company;

    Set<EntityBasic> skills;
}
