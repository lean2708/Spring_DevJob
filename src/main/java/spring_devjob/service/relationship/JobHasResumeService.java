package spring_devjob.service.relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.ApplicationStatusEnum;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.response.ApplyResponse;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.relationship.JobHasResumeRepository;
import spring_devjob.service.AuthService;
import spring_devjob.service.EmailService;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHasResumeService {

    private final JobHasResumeRepository jobHasResumeRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final EmailService emailService;

    public void updateJobHasResumeToInactive(JobHasResume jobHasResume){
        jobHasResume.setState(EntityStatus.INACTIVE);
        jobHasResumeRepository.save(jobHasResume);
    }

    public ApplyResponse applyResumeToJob(Long jobId, Long resumeId) {
        Job job = jobRepository.findById(jobId).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Resume resume = resumeRepository.findById(resumeId).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        if (job.getEndDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.JOB_EXPIRED);
        }

        jobHasResumeRepository.findByJobAndResume(job, resume).ifPresent(
                jobHasResume -> {throw new AppException(ErrorCode.RESUME_ALREADY_APPLIED);});


        JobHasResume jobHasResume = jobHasResumeRepository.save(new JobHasResume(job,resume));

        return ApplyResponse.builder()
                .jobId(jobHasResume.getJob().getId())
                .resumeId(jobHasResume.getResume().getId())
                .userId(resume.getUser().getId())
                .applicationStatus(jobHasResume.getApplicationStatus())
                .appliedAt(jobHasResume.getAppliedAt())
                .build();
    }

    public void updateCVStatus(long jobId, long resumeId, ApplicationStatusEnum applicationStatus) {
        Job job = jobRepository.findById(jobId).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Resume resume = resumeRepository.findById(resumeId).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        JobHasResume jobHasResume = jobHasResumeRepository
                .findByJobAndResume(job,resume).orElseThrow(()-> new AppException(ErrorCode.RESUME_NOT_SUBMITTED));


        jobHasResume.setApplicationStatus(applicationStatus);
        jobHasResumeRepository.save(jobHasResume);

        User hrOrAdmin = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Company company = job.getCompany();

        if (applicationStatus == ApplicationStatusEnum.APPROVED && company != null) {
            emailService.sendResumeApprovedEmail(job, resume, hrOrAdmin);
        } else if (applicationStatus == ApplicationStatusEnum.REJECTED) {
            emailService.sendResumeRejectedEmail(job, resume);
        }
    }
}
