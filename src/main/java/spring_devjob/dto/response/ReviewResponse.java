package spring_devjob.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.basic.UserBasic;

@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse extends BaseResponse {
    double rating;

    String comment;

    UserBasic user;

    EntityBasic company;
}
