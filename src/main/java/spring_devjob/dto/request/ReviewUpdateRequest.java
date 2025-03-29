package spring_devjob.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewUpdateRequest {

    @NotBlank(message = "Name không được để trống")
    String name;

    @Min(value = 0, message = "Rating trong khoảng 0.0-5.0")
    @Max(value = 5, message = "Rating trong khoảng 0.0-5.0")
    double rating;

    String comment;
}
