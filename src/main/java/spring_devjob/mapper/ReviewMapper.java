
package spring_devjob.mapper;

import org.mapstruct.*;
import spring_devjob.dto.request.ReviewCreationRequest;
import spring_devjob.dto.request.ReviewUpdateRequest;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.entity.Review;

import java.util.List;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {SkillMapper.class})
public interface ReviewMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    Review toReview(ReviewCreationRequest request);

    ReviewResponse toReviewResponse(Review review);

    List<ReviewResponse> toReviewResponseList(List<Review> reviews);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateReview(@MappingTarget Review review, ReviewUpdateRequest request);
}
