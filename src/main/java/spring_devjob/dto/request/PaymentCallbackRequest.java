package spring_devjob.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import spring_devjob.entity.Skill;

import java.util.List;
import java.util.Set;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCallbackRequest {
    @NotBlank(message = "Response Code không được để trống")
    String responseCode;
    @Positive(message = "Amount phải lớn hơn 0")
    long amount;
    Set<Long> skillIds;
}
