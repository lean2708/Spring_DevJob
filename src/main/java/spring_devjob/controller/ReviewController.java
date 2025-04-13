package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.ReviewCreationRequest;
import spring_devjob.dto.request.ReviewUpdateRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.service.ReviewService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review",
            description = "API này cho phép người dùng tạo đánh giá (review) cho một công ty")
    @PreAuthorize("hasAuthority('CREATE_REVIEW')")
    @PostMapping("/reviews")
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody ReviewCreationRequest request){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Review")
                .result(reviewService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('GET_REVIEW_BY_ID')")
    @GetMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> fetchReviewById(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                   @PathVariable long id){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Review By Id")
                .result(reviewService.fetchReviewById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('GET_ALL_REVIEWS')")
    @GetMapping("/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                            @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(reviewService.fetchAllReviews(pageNo, pageSize, sortBy))
                .message("Fetch All Reviews With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_REVIEW')")
    @PutMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> update(@Min(value = 1, message = "Id phải lớn hơn 0")
                                            @PathVariable long id,@Valid @RequestBody ReviewUpdateRequest request){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Review By Id")
                .result(reviewService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_REVIEW')")
    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "Id phải lớn hơn 0")
                                    @PathVariable long id){
        reviewService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Review By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_REVIEWS')")
    @DeleteMapping("/reviews")
    public ApiResponse<Void> deleteReviews(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                         Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        reviewService.deleteReviews(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Reviews")
                .result(null)
                .build();
    }
}
