package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.entity.relationship.UserSavedJob;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.*;
import spring_devjob.service.relationship.JobHasResumeService;
import spring_devjob.service.relationship.JobHasSkillService;
import spring_devjob.service.relationship.SavedJobService;
import spring_devjob.service.relationship.UserHasRoleService;

import java.util.List;

import static spring_devjob.constants.EntityStatus.ACTIVE;
import static spring_devjob.constants.EntityStatus.INACTIVE;

@Slf4j
@RequiredArgsConstructor
@Service
public class RestoreService {
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final JobHasResumeService jobHasResumeService;
    private final JobHasSkillService jobHasSkillService;
    private final SavedJobService savedJobService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final UserHasRoleService userHasRoleService;
    private final ResumeRepository resumeRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public CompanyResponse restoreCompany(long id) {
        if(companyRepository.existsById(id)){
            throw new AppException(ErrorCode.COMPANY_ALREADY_ACTIVE);
        }

        Company company = companyRepository.findCompanyById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_EXISTED));

        reviewRepository.updateReviewStateByCompany(company.getId(), ACTIVE.name());

        jobRepository.findJobsByCompanyIdAndState(company.getId(), INACTIVE.name())
                .forEach(job -> restoreJob(job.getId()));

        company.setState(ACTIVE);
        return companyMapper.toCompanyResponse(companyRepository.save(company));
    }

    @Transactional
    public JobResponse restoreJob(long id) {
        if(jobRepository.existsById(id)){
            throw new AppException(ErrorCode.JOB_ALREADY_ACTIVE);
        }
        Job job = jobRepository.findJobById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        List<JobHasResume> jobHasResumeList = jobHasResumeService.getJobHasResumeByJobAndState(job.getId(), INACTIVE);
        jobHasResumeList.forEach(jobHasResume -> jobHasResumeService.updateJobHasResume(jobHasResume, ACTIVE));

        List<JobHasSkill> jobHasSkillList = jobHasSkillService.getJobHasSkillByJobAndState(job.getId(), INACTIVE);
        jobHasSkillList.forEach(jobHasSkill -> jobHasSkillService.updateJobHasSkill(jobHasSkill, ACTIVE));

        List<UserSavedJob> userSavedJobs = savedJobService.getUserSavedJobByJobAndState(job.getId(), INACTIVE);
        userSavedJobs.forEach(userSavedJob -> savedJobService.updateUserSavedJob(userSavedJob, ACTIVE));

        job.setState(ACTIVE);
        return jobMapper.toJobResponse(jobRepository.save(job));
    }


    @Transactional
    public UserResponse restoreUser(long id, EntityStatus oldStatus) {
        if(userRepository.existsById(id)){
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }
        if(userRepository.countById(id, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        User user = userRepository.findUserById(id, oldStatus.name())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        resumeRepository.findByUserIdAndState(user.getId(), INACTIVE.name())
                .forEach(resume -> restoreResume(resume.getId()));

        reviewRepository.updateReviewStateByUser(user.getId(), ACTIVE.name());

        List<UserHasRole> userHasRoleList = userHasRoleService.getUserHasRoleByUserAndState(user.getId(), INACTIVE);
        userHasRoleList.forEach(userHasRole -> userHasRoleService.updateUserHasRole(userHasRole, ACTIVE));

        List<UserSavedJob> userSavedJobList = savedJobService.getUserSavedJobByUserAndState(user.getId(), INACTIVE);
        userSavedJobList.forEach(userSavedJob -> savedJobService.updateUserSavedJob(userSavedJob, ACTIVE));

        user.setState(ACTIVE);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public ResumeResponse restoreResume(long id) {
        if(resumeRepository.existsById(id)){
            throw new AppException(ErrorCode.RESUME_ALREADY_ACTIVE);
        }
        Resume resume = resumeRepository.findResumeById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        List<JobHasResume> jobHasResumeList = jobHasResumeService.getJobHasResumeByResumeAndState(resume.getId(), INACTIVE);
        jobHasResumeList.forEach(jobHasResume -> jobHasResumeService.updateJobHasResume(jobHasResume, ACTIVE));

        resume.setState(ACTIVE);
        return resumeMapper.toResumeResponse(resumeRepository.save(resume));
    }
}
