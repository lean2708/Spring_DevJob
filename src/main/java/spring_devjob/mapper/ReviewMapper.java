
package spring_devjob.mapper;

import org.mapstruct.*;
import org.springframework.context.annotation.DependsOn;
import spring_devjob.dto.request.ReviewRequest;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.Review;
import spring_devjob.entity.User;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {SkillMapper.class})
public interface ReviewMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    Review toReview(ReviewRequest request);

    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateReview(@MappingTarget Review review, ReviewRequest request);
}
