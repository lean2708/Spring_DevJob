package spring_devjob.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import spring_devjob.constants.LevelEnum;
import spring_devjob.dto.validator.EnumPattern;

import java.time.LocalDate;
import java.util.Set;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobUpdateRequest {

    String name;
    Boolean jobStatus;
    String location;
    Double salary;
    Integer quantity;
    LevelEnum level;

    String description;


    LocalDate startDate;
    LocalDate endDate;

    Set<Long> skillIds;
}
