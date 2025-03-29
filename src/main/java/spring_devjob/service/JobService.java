package spring_devjob.service;

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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spring_devjob.dto.request.JobCreationRequest;
import spring_devjob.dto.request.JobUpdateRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.repository.*;
import spring_devjob.repository.criteria.JobSearchCriteriaQueryConsumer;
import spring_devjob.repository.criteria.SearchCriteria;
import spring_devjob.repository.relationship.JobHasSkillRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;
    private final ResumeRepository resumeRepository;
    private final PageableService pageableService;
    private final ResumeMapper resumeMapper;
    private final JobHasSkillRepository jobHasSkillRepository;
    private final EntityDeactivationService entityDeactivationService;
    @PersistenceContext
    private EntityManager entityManager;

    public JobResponse create(JobCreationRequest request){
        Company company = companyRepository.findById(request.getCompanyId()).
                orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        if(jobRepository.existsByNameAndCompanyId(request.getName(), company.getId())){
            throw new AppException(ErrorCode.JOB_ALREADY_EXISTS_IN_COMPANY);
        }

        Job job = jobMapper.toJob(request);

        job.setCompany(company);

        jobRepository.save(job);

        if(!CollectionUtils.isEmpty(request.getSkillIds())){
            Set<Skill> skillSet = skillRepository.findAllByIdIn(request.getSkillIds());

            Set<JobHasSkill> jobHasSkills = skillSet.stream()
                            .map(skill -> new JobHasSkill(job,skill))
                            .collect(Collectors.toSet());

            job.setSkills(new HashSet<>(jobHasSkillRepository.saveAll(jobHasSkills)));
        }

        return jobMapper.toJobResponse(job);
    }

    public JobResponse fetchJob(long id){
        Job jobDB = findActiveJobById(id);

        return jobMapper.toJobResponse(jobDB);
    }

    public PageResponse<JobResponse> fetchAllJobs(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Job> jobPage = jobRepository.findAll(pageable);

        return PageResponse.<JobResponse>builder()
                .page(jobPage.getNumber() + 1)
                .size(jobPage.getSize())
                .totalPages(jobPage.getTotalPages())
                .totalItems(jobPage.getTotalElements())
                .items(jobMapper.toJobResponseList(jobPage.getContent()))
                .build();
    }

    @Transactional
    public JobResponse update(long id, JobUpdateRequest request){
        Job jobDB = findActiveJobById(id);

        jobMapper.updateJob(jobDB, request);

        if(!CollectionUtils.isEmpty(request.getSkillIds())){
            Set<Skill> skillSet = skillRepository.findAllByIdIn(request.getSkillIds());

            jobHasSkillRepository.deleteByJob(jobDB);

            Set<JobHasSkill> jobHasSkills = skillSet.stream()
                    .map(skill -> new JobHasSkill(jobDB,skill))
                    .collect(Collectors.toSet());

            jobDB.setSkills(new HashSet<>(jobHasSkillRepository.saveAll(jobHasSkills)));
        }

        return jobMapper.toJobResponse(jobRepository.save(jobDB));
    }

    @Transactional
    public void delete(long id){
        Job jobDB = findActiveJobById(id);

        entityDeactivationService.deactivateJob(jobDB);
    }


    @Transactional
    public void deleteJobs(Set<Long> ids){
        Set<Job> jobSet = jobRepository.findAllByIdIn(ids);
        if(jobSet.isEmpty()){
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }
        jobSet.forEach(entityDeactivationService::deactivateJob);
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
                .items(jobMapper.toJobResponseList(jobs))
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
            Join<Job, JobHasSkill> jobSkillJoin = root.join("skills", JoinType.INNER);
            Join<JobHasSkill, Skill> skillJoin = jobSkillJoin.join("skill", JoinType.INNER);
            List<Predicate> skillPredicateList = new ArrayList<>();
            for(String nameSkill : skills){
                skillPredicateList.add(builder.like(skillJoin.get("name"), "%" + nameSkill + "%"));
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
            Join<Job, JobHasSkill> jobSkillJoin = root.join("skills", JoinType.INNER);
            Join<JobHasSkill, Skill> skillJoin = jobSkillJoin.join("skill", JoinType.INNER);
            List<Predicate> skillPredicateList = new ArrayList<>();
            for(String nameSkill : skills){
                skillPredicateList.add(builder.like(skillJoin.get("name"), "%" + nameSkill + "%"));
            }
            Predicate skillPredicate = builder.or(skillPredicateList.toArray(new Predicate[0]));
            predicate = builder.and(predicate, skillPredicate);
        }

        countQuery.select(builder.count(root));
        countQuery.where(predicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    public PageResponse<ResumeResponse> getResumesByJob(int pageNo, int pageSize, String sortBy, long jobId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Page<Resume> resumePage = resumeRepository.findAllByJob(job, pageable);

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)  // Thêm 1 để bắt đầu từ trang 1
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(resumeMapper.toResumeResponseList(resumePage.getContent()))
                .build();
    }

    private Job findActiveJobById(long id) {
        return jobRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredJobs() {
        jobRepository.updateExpiredJobs(LocalDate.now());
    }

}
