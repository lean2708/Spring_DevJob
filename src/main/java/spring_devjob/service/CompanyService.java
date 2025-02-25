package spring_devjob.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Skill;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.repository.CompanyRepository;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.criteria.JobSearchCriteriaQueryConsumer;
import spring_devjob.repository.criteria.SearchCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PageableService pageableService;
    @PersistenceContext
    private EntityManager entityManager;

    public CompanyResponse create(CompanyRequest request){
        if(companyRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.COMPANY_EXISTED);
        }
        Company company = companyMapper.toCompany(request);

        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    public CompanyResponse fetchCompany(long id){
        Company companyDB = companyRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        return companyMapper.toCompanyResponse(companyDB);
    }

    public PageResponse<CompanyResponse> fetchAllCompanies(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<CompanyResponse> responses =  convertListCompanyResponse(companyPage.getContent());

        return PageResponse.<CompanyResponse>builder()
                .page(companyPage.getNumber() + 1)
                .size(companyPage.getSize())
                .totalPages(companyPage.getTotalPages())
                .totalItems(companyPage.getTotalElements())
                .items(responses)
                .build();
    }

    public CompanyResponse update(long id, CompanyRequest request){
        Company companyDB = companyRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        companyMapper.updateCompany(companyDB, request);

        return companyMapper.toCompanyResponse(companyRepository.save(companyDB));
    }

    public void delete(long id){
        Company companyDB = companyRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        userRepository.deleteAll(companyDB.getUsers());
        jobRepository.deleteAll(companyDB.getJobs());

        companyRepository.delete(companyDB);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCompanies(List<Long> ids) {
        List<Company> companyList = companyRepository.findAllByIdIn(ids);
        if(companyList.isEmpty()){
            throw new AppException(ErrorCode.COMPANY_NOT_FOUND);
        }
        userRepository.deleteAllByCompanyIn(companyList);
        jobRepository.deleteAllByCompanyIn(companyList);

        companyRepository.deleteAllInBatch(companyList);
    }

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
                .items(convertListCompanyResponse(companies))
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


    public List<CompanyResponse> convertListCompanyResponse(List<Company> companyList){
        List<CompanyResponse> companyResponseList = new ArrayList<>();
        for(Company company : companyList){
            CompanyResponse response = companyMapper.toCompanyResponse(company);
            companyResponseList.add(response);
        }
        return companyResponseList;
    }
}
