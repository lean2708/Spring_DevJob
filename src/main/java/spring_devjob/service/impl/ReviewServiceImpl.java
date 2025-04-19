package spring_devjob.service.impl;

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
import spring_devjob.service.AuthService;
import spring_devjob.service.CurrentUserService;
import spring_devjob.service.PageableService;
import spring_devjob.service.ReviewService;

import java.util.List;
import java.util.Set;

@Slf4j(topic = "REVIEW-SERVICE")
@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PageableService pageableService;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;


    @Override
    public ReviewResponse create(ReviewCreationRequest request) {
        Company company = companyRepository.findById(request.getCompanyId()).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
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

    @Override
    public ReviewResponse fetchReviewById(long id) {
        Review review = findActiveReviewById(id);

        return reviewMapper.toReviewResponse(review);
    }

    @Override
    public PageResponse<ReviewResponse> fetchAllReviews(int pageNo, int pageSize, String sortBy) {
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy, Review.class);

        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return PageResponse.<ReviewResponse>builder()
                .page(reviewPage.getNumber() + 1)
                .size(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .items(reviewMapper.toReviewResponseList(reviewPage.getContent()))
                .build();
    }

    @Override
    public ReviewResponse update(long id, ReviewUpdateRequest request) {
        Review review = findActiveReviewById(id);

        reviewMapper.updateReview(review, request);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    @Override
    public void delete(long id) {
        Review review = findActiveReviewById(id);

        reviewRepository.delete(review);
    }

    @Transactional
    @Override
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
