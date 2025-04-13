package spring_devjob.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import spring_devjob.constants.LevelEnum;
import spring_devjob.dto.validator.EnumPattern;

import java.time.LocalDate;
import java.util.Set;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobCreationRequest {
    @NotBlank(message = "Name không được để trống")
    String name;
    Boolean jobStatus;
    @NotBlank(message = "Location không được để trống")
    String location;
    @Min(value = 0, message = "salary phải lớn hơn 0")
    @NotNull(message = "salary không được null")
    Double salary;
    @Min(value = 0, message = "quantity phải lớn hơn 0")
    @NotNull(message = "quantity không được null")
    Integer quantity;
    @NotNull(message = "Level không được để trống")
    @EnumPattern(name = "level", regexp = "INTERN|FRESHER|JUNIOR|MIDDLE|SENIOR")
    LevelEnum level;

    String description;

    LocalDate startDate;
    LocalDate endDate;

    @Min(value = 0, message = "companyId phải lớn hơn 0")
    @NotNull(message = "companyId không được null")
    Long companyId;

    @NotEmpty(message = "Skills không được để trống")
    Set<Long> skillIds;
}
