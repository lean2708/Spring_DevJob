package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.dto.request.ReviewCreationRequest;
import spring_devjob.dto.request.ReviewUpdateRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.ReviewMapper;
import spring_devjob.repository.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final CompanyRepository companyRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PageableService pageableService;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;


    public ReviewResponse create(ReviewCreationRequest request) {
        Company company = companyRepository.findById(request.getCompanyId()).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<Resume> resumeSet = resumeRepository.findAllByUserId(user.getId());

        List<Job> jobList = jobRepository.findAllByCompanyIdAndResumesIn(company.getId(), resumeSet);

        if(jobList.isEmpty()){
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        Review review = reviewMapper.toReview(request);
        review.setUser(user);
        review.setCompany(company);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    public ReviewResponse fetchReviewById(long id) {
        Review review = findActiveReviewById(id);

        return reviewMapper.toReviewResponse(review);
    }

    public PageResponse<ReviewResponse> fetchAllReviews(int pageNo, int pageSize, String sortBy) {
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return PageResponse.<ReviewResponse>builder()
                .page(reviewPage.getNumber() + 1)
                .size(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .items(reviewMapper.toReviewResponseList(reviewPage.getContent()))
                .build();
    }

    public ReviewResponse update(long id, ReviewUpdateRequest request) {
        Review review = findActiveReviewById(id);

        reviewMapper.updateReview(review, request);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(long id) {
        Review review = findActiveReviewById(id);

        reviewRepository.delete(review);
    }

    @Transactional
    public void deleteReviews(Set<Long> ids) {
        Set<Review> reviewSet = reviewRepository.findAllByIdIn(ids);

        if(reviewSet.isEmpty()){
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        reviewRepository.deleteAllInBatch(reviewSet);
    }

    private Review findActiveReviewById(long id) {
        return reviewRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_EXISTED));
    }

}
