package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.constants.LevelEnum;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.SkillBasic;

import java.time.LocalDate;
import java.util.List;
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
    boolean status;

    CompanyBasic company;

    List<SkillBasic> skills;
}
