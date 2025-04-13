package spring_devjob.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor(force = true)
public class CompanyResponse extends BaseResponse {
    String name;
    String description;
    String address;
    String logoUrl;

    Double averageRating;
    Integer totalReviews;

    String createdBy;
    String updatedBy;
    LocalDate createdAt;
    LocalDate updatedAt;

}
