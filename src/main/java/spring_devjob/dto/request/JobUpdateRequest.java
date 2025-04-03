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
    @NotBlank(message = "Name không được để trống")
    String name;
    boolean jobStatus;
    @NotBlank(message = "Location không được để trống")
    String location;
    @Positive(message = "Salary phải lớn hơn 0")
    Double salary;
    @Positive(message = "Quantity phải lớn hơn 0")
    Integer quantity;
    @NotNull(message = "Level không được để trống")
    @EnumPattern(name = "level", regexp = "INTERN|FRESHER|JUNIOR|MIDDLE|SENIOR")
    LevelEnum level;

    String description;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "25/03/2025")
    LocalDate startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy")
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "25/03/2025")
    LocalDate endDate;

    @NotEmpty(message = "Skill không được để trống")
    Set<Long> skillIds;
}
