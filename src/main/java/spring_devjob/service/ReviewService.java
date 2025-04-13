package spring_devjob.service;

import spring_devjob.dto.request.ReviewCreationRequest;
import spring_devjob.dto.request.ReviewUpdateRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;

import java.util.Set;

public interface ReviewService {

    ReviewResponse create(ReviewCreationRequest request);

    ReviewResponse fetchReviewById(long id);

    PageResponse<ReviewResponse> fetchAllReviews(int pageNo, int pageSize, String sortBy);

    ReviewResponse update(long id, ReviewUpdateRequest request);

    void delete(long id);

    void deleteReviews(Set<Long> ids);
}
