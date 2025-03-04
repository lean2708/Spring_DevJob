package spring_devjob.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.entity.Skill;


import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriberResponse extends BaseResponse {
    String email;

    LocalDate startDate;
    LocalDate expiryDate;

    List<String> skills;
}
