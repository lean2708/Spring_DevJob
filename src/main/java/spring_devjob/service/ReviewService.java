package spring_devjob.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.ReviewRequest;
import spring_devjob.dto.request.RoleRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;
import spring_devjob.dto.response.RoleResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.ReviewMapper;
import spring_devjob.repository.CompanyRepository;
import spring_devjob.repository.ReviewRepository;
import spring_devjob.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

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


    public ReviewResponse create(ReviewRequest request) {
        Review review = reviewMapper.toReview(request);

        Company company = companyRepository.findById(request.getCompanyId()).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        review.setCompany(company);
        
        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        review.setUser(user);
        
        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    public ReviewResponse fetchReviewById(long id) {
        Review review = findActiveResumeById(id);

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

    public ReviewResponse update(long id, ReviewRequest request) {
        Review review = findActiveResumeById(id);

        Company company = companyRepository.findById(request.getCompanyId()).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
        review.setCompany(company);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(long id) {
        Review review = findActiveResumeById(id);

        deactivateReview(review);

        reviewRepository.save(review);
    }

    private void deactivateReview(Review review) {
        review.setState(EntityStatus.INACTIVE);
        review.setDeactivatedAt(LocalDate.now());
    }

    @Transactional
    public void deleteReviews(Set<Long> ids) {
        Set<Review> reviewSet = reviewRepository.findAllByIdIn(ids);

        if(reviewSet.isEmpty()){
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        reviewSet.forEach(this::deactivateReview);

        reviewRepository.saveAll(reviewSet);
    }

    public PageResponse<ReviewResponse> getReviewsByCompany(int pageNo, int pageSize, String sortBy, long companyId) {
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Company company = companyRepository.findById(companyId).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        Page<Review> reviewPage = reviewRepository.findAllByCompanyId(pageable, company.getId());

        return PageResponse.<ReviewResponse>builder()
                .page(reviewPage.getNumber() + 1)
                .size(reviewPage.getSize())
                .totalPages(reviewPage.getTotalPages())
                .totalItems(reviewPage.getTotalElements())
                .items(reviewMapper.toReviewResponseList(reviewPage.getContent()))
                .build();
    }

    private Review findActiveResumeById(long id) {
        return reviewRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.REVIEW_NOT_EXISTED));
    }

}
