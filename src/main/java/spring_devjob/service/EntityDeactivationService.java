package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.entity.relationship.UserSavedJob;
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
public class EntityDeactivationService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ReviewRepository reviewRepository;
    private final ResumeRepository resumeRepository;
    private final UserHasRoleService userHasRoleService;
    private final SavedJobService savedJobService;
    private final JobHasResumeService jobHasResumeService;
    private final JobHasSkillService jobHasSkillService;
    private final CompanyRepository companyRepository;


    @Transactional
    public void deactivateCompany(Company company){
        userRepository.updateUserStateByCompany(company.getId(), INACTIVE.name());

        reviewRepository.updateReviewStateByCompany(company.getId(), INACTIVE.name());

        jobRepository.findJobsByCompanyIdAndState(company.getId(), ACTIVE.name()).forEach(this::deactivateJob);

        company.setState(INACTIVE);
        companyRepository.saveAndFlush(company);
    }

    @Transactional
    public void deactivateJob(Job job){
        List<JobHasResume> jobHasResumeList = jobHasResumeService.getJobHasResumeByJobAndState(job.getId(), ACTIVE);
        jobHasResumeList.forEach(jobHasResume -> jobHasResumeService.updateJobHasResume(jobHasResume, INACTIVE));

        List<JobHasSkill> jobHasSkillList = jobHasSkillService.getJobHasSkillByJobAndState(job.getId(), ACTIVE);
        jobHasSkillList.forEach(jobHasSkill -> jobHasSkillService.updateJobHasSkill(jobHasSkill, INACTIVE));

        List<UserSavedJob> userSavedJobs = savedJobService.getUserSavedJobByJobAndState(job.getId(), ACTIVE);
        userSavedJobs.forEach(userSavedJob -> savedJobService.updateUserSavedJob(userSavedJob, INACTIVE));

        job.setState(INACTIVE);
        jobRepository.save(job);
    }

    @Transactional
    public void deactivateUser(User user) {
        resumeRepository.findByUserId(user.getId()).forEach(this::deactivateResume);

        reviewRepository.updateReviewStateByUser(user.getId(), INACTIVE.name());

        List<UserHasRole> userHasRoleList = userHasRoleService.getUserHasRoleByUserAndState(user.getId(), ACTIVE);
        userHasRoleList.forEach(userHasRole -> userHasRoleService.updateUserHasRole(userHasRole, INACTIVE));

        List<UserSavedJob> userSavedJobList = savedJobService.getUserSavedJobByUserAndState(user.getId(), ACTIVE);
        userSavedJobList.forEach(userSavedJob -> savedJobService.updateUserSavedJob(userSavedJob, INACTIVE));

        user.setState(INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateResume(Resume resume){
        List<JobHasResume> jobHasResumeList = jobHasResumeService.getJobHasResumeByResumeAndState(resume.getId(), ACTIVE);
        jobHasResumeList.forEach(jobHasResume -> jobHasResumeService.updateJobHasResume(jobHasResume, INACTIVE));

        resume.setState(INACTIVE);
        resumeRepository.save(resume);
    }
}
