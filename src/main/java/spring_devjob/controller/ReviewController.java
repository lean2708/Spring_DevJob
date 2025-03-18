package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.ReviewRequest;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.dto.response.RoleResponse;
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
    @PostMapping("/reviews")
    public ApiResponse<ReviewResponse> create(@Valid @RequestBody ReviewRequest request){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Review")
                .result(reviewService.create(request))
                .build();
    }

    @GetMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> fetchReviewById(@Positive(message = "ID phải lớn hơn 0")
                                                   @PathVariable long id){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Review By Id")
                .result(reviewService.fetchReviewById(id))
                .build();
    }

    @GetMapping("/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                            @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                            @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                            @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(reviewService.fetchAllReviews(pageNo, pageSize, sortBy))
                .message("Fetch All Reviews With Pagination")
                .build();
    }

    @PutMapping("/reviews/{id}")
    public ApiResponse<ReviewResponse> update(@Positive(message = "ID phải lớn hơn 0")
                                            @PathVariable long id, @RequestBody ReviewRequest request){
        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Review By Id")
                .result(reviewService.update(id, request))
                .build();
    }

    @DeleteMapping("/reviews/{id}")
    public ApiResponse<Void> delete(@Positive(message = "ID phải lớn hơn 0")
                                    @PathVariable long id){
        reviewService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Review By Id")
                .result(null)
                .build();
    }

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
