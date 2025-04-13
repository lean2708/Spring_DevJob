package spring_devjob.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewCreationRequest {

    @NotBlank(message = "Name không được để trống")
    String name;

    @NotNull(message = "rating không được null")
    @Min(value = 0, message = "Rating trong khoảng 0.0-5.0")
    @Max(value = 5, message = "Rating trong khoảng 0.0-5.0")
    Double rating;

    @NotBlank(message = "comment không được để trống")
    String comment;

    @NotNull(message = "companyId không được null")
    @Min(value = 0, message = "companyId pha lớn hơn 0")
    Long companyId;
}
