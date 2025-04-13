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
    String name;
    String location;
    double salary;
    int quantity;
    LevelEnum level;

    String description;

    LocalDate startDate;
    LocalDate endDate;
    Boolean jobStatus;

    String createdBy;
    String updatedBy;
    LocalDate createdAt;
    LocalDate updatedAt;

    EntityBasic company;

    Set<EntityBasic> skills;
}
