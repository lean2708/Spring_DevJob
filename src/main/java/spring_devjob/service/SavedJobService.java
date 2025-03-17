package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.UserSavedJob;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.JobMapper;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.UserSavedJobRepository;
import spring_devjob.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserSavedJobRepository userSavedJobRepository;
    private final PageableService pageableService;
    private final JobMapper jobMapper;

    public void saveJob(long jobId){
        Job job = jobRepository.findById(jobId).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userSavedJobRepository.save(UserSavedJob.builder()
                .user(user)
                .job(job)
                .savedAt(LocalDate.now())
                .build());
    }

    public PageResponse<JobResponse> getSavedJobs(int pageNo, int pageSize, String sortBy) {
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<UserSavedJob> userHasJobs = userSavedJobRepository.findAllByUserId(pageable, user.getId());

        List<JobResponse> jobResponses = userHasJobs.stream()
                .map(userHasJob -> jobMapper.toJobResponse(userHasJob.getJob()))
                .toList();

        return PageResponse.<JobResponse>builder()
                .page(userHasJobs.getNumber() + 1)
                .size(userHasJobs.getSize())
                .totalPages(userHasJobs.getTotalPages())
                .totalItems(userHasJobs.getTotalElements())
                .items(jobResponses)
                .build();
    }

    public void removeSaveJob(long jobId) {
        Job job = jobRepository.findById(jobId).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserSavedJob userHasJob = userSavedJobRepository.findByUserIdAndJobId(user.getId(), job.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_SAVED));

        userSavedJobRepository.delete(userHasJob);
    }

    public void updateUserSavedJobToInactive(UserSavedJob userSavedJob){
        userSavedJob.setState(EntityStatus.INACTIVE);
        userSavedJobRepository.save(userSavedJob);
    }

}
