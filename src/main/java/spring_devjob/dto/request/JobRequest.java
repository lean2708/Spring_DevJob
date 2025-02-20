package spring_devjob.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import spring_devjob.constants.LevelEnum;
import spring_devjob.dto.validator.EnumPattern;

import java.time.LocalDate;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobRequest {
    @NotBlank(message = "Name không được để trống")
    String name;
    boolean status;
    @NotBlank(message = "Location không được để trống")
    String location;
    @Positive(message = "Salary phải lớn hơn 0")
    double salary;
    int quantity;
    @NotNull(message = "Level không được để trống")
    @EnumPattern(name = "level", regexp = "INTERN|FRESHER|JUNIOR|MIDDLE|SENIOR")
    LevelEnum level;

    String description;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate startDate;
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy")
    LocalDate endDate;

    String company;

    List<String> skills;
}
