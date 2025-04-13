package spring_devjob.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.*;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ReviewMapper;
import spring_devjob.repository.CompanyRepository;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.ReviewRepository;
import spring_devjob.repository.criteria.JobSearchCriteriaQueryConsumer;
import spring_devjob.repository.criteria.SearchCriteria;
import spring_devjob.service.CompanyService;
import spring_devjob.service.EntityDeactivationService;
import spring_devjob.service.PageableService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j(topic = "COMPANY-SERVICE")
@RequiredArgsConstructor
@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final EntityDeactivationService entityDeactivationService;
    private final JobRepository jobRepository;
    private final PageableService pageableService;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final JobMapper jobMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public CompanyResponse create(CompanyRequest request){
        if(companyRepository.countByNameAndState(request.getName(),  EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }

        if(companyRepository.countByNameAndState(request.getName(), EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.COMPANY_DISABLED);
        }

        Company company = companyMapper.toCompany(request);

        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Override
    public CompanyResponse fetchCompany(long id){
        Company companyDB = findActiveCompanyById(id);

        return companyMapper.toCompanyResponse(companyDB);
    }

    @Override
    public PageResponse<CompanyResponse> fetchAllCompanies(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Company> companyPage = companyRepository.findAll(pageable);

        return PageResponse.<CompanyResponse>builder()
                .page(companyPage.getNumber() + 1)
                .size(companyPage.getSize())
                .totalPages(companyPage.getTotalPages())
                .totalItems(companyPage.getTotalElements())
                .items(companyMapper.toCompanyResponseList(companyPage.getContent()))
                .build();
    }

    @Override
    public CompanyResponse update(long id, CompanyRequest request){
        if(companyRepository.countByNameAndState(request.getName(),  EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }

        if(companyRepository.countByNameAndState(request.getName(), EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.COMPANY_DISABLED);
        }

        Company companyDB = findActiveCompanyById(id);

        companyMapper.updateCompany(companyDB, request);

        return companyMapper.toCompanyResponse(companyRepository.save(companyDB));
    }

    @Transactional
    @Override
    public void delete(long id){
        Company companyDB = findActiveCompanyById(id);

        entityDeactivationService.deactivateCompany(companyDB);

    }

    @Transactional
    @Override
    public void deleteCompanies(Set<Long> ids) {
        Set<Company> companySet = companyRepository.findAllByIdIn(ids);
        if(companySet.isEmpty()){
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }
        companySet.forEach(entityDeactivationService::deactivateCompany);
    }


    @Override
    public PageResponse<CompanyResponse> searchCompany(int pageNo, int pageSize, String sortBy, List<String> search){
        pageNo = pageNo - 1;

        List<SearchCriteria> criteriaList = new ArrayList<>();

        // lay danh sach cac dieu kien search
        if(search != null){
            for(String s : search){
                Pattern pattern = Pattern.compile("(\\w+?)(~|>|<)(.*)");
                Matcher matcher = pattern.matcher(s);
                if(matcher.find()){
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        List<Company> companies = getCompanies(pageNo, pageSize, sortBy, criteriaList);

        // tong so phan tu
        Long totalElements = getTotalElements(criteriaList);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return PageResponse.<CompanyResponse>builder()
                .page(pageNo + 1)
                .size(pageSize)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .items(companyMapper.toCompanyResponseList(companies))
                .build();
    }


    private List<Company> getCompanies(int pageNo, int pageSize, String sortBy, List<SearchCriteria> criteriaList){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Company> query = builder.createQuery(Company.class);
        Root<Company> root = query.from(Company.class);

        // Xu ly dieu kien tim kiem
        Predicate predicate = builder.conjunction();

        if(criteriaList != null && !criteriaList.isEmpty()){ // search job
            JobSearchCriteriaQueryConsumer queryConsumer = new JobSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = builder.and(predicate, queryConsumer.getPredicate());
        }

        query.where(predicate);

        // Sort
        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(-)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if(matcher.group(3).equalsIgnoreCase("desc")){
                    query.orderBy(builder.desc(root.get(columnName)));
                }else{
                    query.orderBy(builder.asc(root.get(columnName)));
                }
            }
        }

        return entityManager.createQuery(query)
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Company> root = countQuery.from(Company.class);

        // Xu ly dieu kien tim kiem
        Predicate predicate = builder.conjunction();

        if(criteriaList != null && !criteriaList.isEmpty()){ // search job
            JobSearchCriteriaQueryConsumer queryConsumer = new JobSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = builder.and(predicate, queryConsumer.getPredicate());
        }

        countQuery.select(builder.count(root));
        countQuery.where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public PageResponse<JobResponse> getAllJobsByCompany(int pageNo, int pageSize, String sortBy, long companyId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Company companyDB = findActiveCompanyById(companyId);

        Page<Job> jobPage = jobRepository.findAllByCompanyId(companyDB.getId(), pageable);

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(jobMapper.toJobResponseList(jobPage.getContent()))
                .build();
    }

    @Override
    public PageResponse<CompanyResponse> getTopRatedCompanies(int pageNo, int pageSize, double rating){
        return searchCompany(pageNo, pageSize, "rating-desc", List.of("rating>=" + rating));
    }

    @Override
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

    @Scheduled(cron = "0 0 0 */7 * ?") // Chạy mỗi 7 ngày vào lúc 00:00
    public void updateCompanyRatings() {
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            double avgRating = reviewRepository.getAverageRatingByCompanyId(company.getId());
            int totalReviews = reviewRepository.getTotalReviewsByCompanyId(company.getId());
            company.setAverageRating(avgRating);
            company.setTotalReviews(totalReviews);
            companyRepository.save(company);
        }
        log.info("Update Companies Ratings");
    }

    private Company findActiveCompanyById(long id) {
        return companyRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));
    }


}
