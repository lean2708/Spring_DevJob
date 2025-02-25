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
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.basic.SkillBasic;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.SkillMapper;
import spring_devjob.repository.*;
import spring_devjob.repository.criteria.JobSearchCriteriaQueryConsumer;
import spring_devjob.repository.criteria.SearchCriteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PageableService pageableService;
    private final AuthService authService;
    private final SkillMapper skillMapper;
    @PersistenceContext
    private EntityManager entityManager;

    public JobResponse create(JobRequest request){
        if(jobRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.JOB_EXISTED);
        }
        Job job = jobMapper.toJob(request);

        if(request.getCompany() != null && !request.getCompany().isEmpty()){
            Company company = companyRepository.findByName(request.getCompany());
            job.setCompany(company);
        }

        if(request.getSkills() != null && !request.getSkills().isEmpty()){
            List<Skill> skills = skillRepository.findAllByNameIn(request.getSkills());
            job.setSkills(skills);
        }

        return convertJobResponse(jobRepository.save(job));
    }

    public JobResponse fetchJob(long id){
        Job jobDB = jobRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        return convertJobResponse(jobDB);
    }

    public PageResponse<JobResponse> fetchAllJobs(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Job> jobPage = jobRepository.findAll(pageable);

        List<JobResponse> responses =  convertListJobResponse(jobPage.getContent());

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(responses)
                .build();
    }

    public JobResponse update(long id, JobRequest request){
        Job jobDB = jobRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        jobMapper.updateJob(jobDB, request);

        if(request.getCompany() != null && !request.getCompany().isEmpty()){
            Company company = companyRepository.findByName(request.getCompany());
            jobDB.setCompany(company);
        }

        if(request.getSkills() != null && !request.getSkills().isEmpty()){
            List<Skill> skills = skillRepository.findAllByNameIn(request.getSkills());
            jobDB.setSkills(skills);
        }

        return convertJobResponse(jobRepository.save(jobDB));
    }

    public void delete(long id){
        Job jobDB = jobRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        jobDB.getResumes().forEach(resume -> {
            resume.setJob(null);
            resumeRepository.save(resume);
        });

        jobRepository.delete(jobDB);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteJobs(List<Long> ids){
        List<Job> jobList = jobRepository.findAllByIdIn(ids);
        if(jobList.isEmpty()){
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }
        for(Job job : jobList){
            job.getResumes().forEach(resume -> {
                resume.setJob(null);
                resumeRepository.save(resume);
            });
        }
        jobRepository.deleteAllInBatch(jobList);
    }

    public PageResponse<JobResponse> fetchAllJobsBySkills(int pageNo, int pageSize, String sortBy, List<String> search, List<String> skills){
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

        List<Job> jobs = getJobsBySkills(pageNo, pageSize, sortBy, criteriaList, skills);

        // tong so phan tu
        Long totalElements = getTotalElements(criteriaList, skills);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return PageResponse.<JobResponse>builder()
                .page(pageNo + 1)
                .size(pageSize)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .items(convertListJobResponse(jobs))
                .build();
    }

    private List<Job> getJobsBySkills(int pageNo, int pageSize, String sortBy, List<SearchCriteria> criteriaList, List<String> skills){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Job> query = builder.createQuery(Job.class);
        Root<Job> root = query.from(Job.class);

        // Xu ly dieu kien tim kiem
        Predicate predicate = builder.conjunction();

        if(criteriaList != null && !criteriaList.isEmpty()){ // search job
            JobSearchCriteriaQueryConsumer queryConsumer = new JobSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = builder.and(predicate, queryConsumer.getPredicate());
        }

        if(skills != null && !skills.isEmpty()){ // search name skill
            Join<Job, Skill> skillJobJoin = root.join("skills", JoinType.LEFT);
            List<Predicate> skillPredicateList = new ArrayList<>();
            for(String nameSkill : skills){
                skillPredicateList.add(builder.like(skillJobJoin.get("name"), "%" + nameSkill + "%"));
            }
            Predicate skillPredicate = builder.or(skillPredicateList.toArray(new Predicate[0]));
            predicate = builder.and(predicate, skillPredicate);
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

    private Long getTotalElements(List<SearchCriteria> criteriaList, List<String> skills){
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Job> root = countQuery.from(Job.class);

        // Xu ly dieu kien tim kiem
        Predicate predicate = builder.conjunction();

        if(criteriaList != null && !criteriaList.isEmpty()){ // search job
            JobSearchCriteriaQueryConsumer queryConsumer = new JobSearchCriteriaQueryConsumer(builder, predicate, root);
            criteriaList.forEach(queryConsumer);
            predicate = builder.and(predicate, queryConsumer.getPredicate());
        }

        if(skills != null && !skills.isEmpty()){ // search name skill
            Join<Job, Skill> skillJobJoin = root.join("skills", JoinType.LEFT);
            List<Predicate> skillPredicateList = new ArrayList<>();
            for(String nameSkill : skills){
                skillPredicateList.add(builder.like(skillJobJoin.get("name"), "%" + nameSkill + "%"));
            }
            Predicate skillPredicate = builder.or(skillPredicateList.toArray(new Predicate[0]));
            predicate = builder.and(predicate, skillPredicate);
        }

        countQuery.select(builder.count(root));
        countQuery.where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public PageResponse<JobResponse> getAllAppliedJobsByUser(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Resume> resumes = resumeRepository.findAllByUserId(user.getId());

        Page<Job> jobPage = jobRepository.findAllByResumesIn(resumes, pageable);

        List<JobResponse> responses =  convertListJobResponse(jobPage.toList());

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(responses)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public PageResponse<JobResponse> getAllJobsByCompany(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getCompany() == null){
            throw new AppException(ErrorCode.COMPANY_NOT_ASSOCIATED);
        }

        Page<Job> jobPage = jobRepository.findAllByCompanyId(user.getCompany().getId(), pageable);

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(convertListJobResponse(jobPage.getContent()))
                .build();
    }

    public List<JobResponse> convertListJobResponse(List<Job> jobList){
        List<JobResponse> jobResponseList = new ArrayList<>();
        for(Job job : jobList){
            JobResponse response = convertJobResponse(job);
            jobResponseList.add(response);
        }
        return jobResponseList;
    }

    public JobResponse convertJobResponse(Job job){
        JobResponse response = jobMapper.toJobResponse(job);

        CompanyBasic companyBasic = (job.getCompany() != null) ?
                companyMapper.toCompanyBasic(job.getCompany()) : null;
        response.setCompany(companyBasic);

        List<SkillBasic> skillBasics = (job.getSkills() != null) ?
                skillMapper.toSkillBasics(job.getSkills()) : null;
        response.setSkills(skillBasics);

        return response;
    }
}
