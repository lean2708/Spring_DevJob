package spring_devjob.service.relationship;

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
import spring_devjob.repository.relationship.UserSavedJobRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.service.AuthService;
import spring_devjob.service.CurrentUserService;
import spring_devjob.service.PageableService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final UserSavedJobRepository userSavedJobRepository;
    private final PageableService pageableService;
    private final JobMapper jobMapper;

    public void saveJob(long jobId){
        Job job = jobRepository.findById(jobId).
                orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userSavedJobRepository.save(new UserSavedJob(job, user));
    }

    public PageResponse<JobResponse> getSavedJobs(int pageNo, int pageSize, String sortBy) {
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy, UserSavedJob.class);

        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
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

        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserSavedJob userHasJob = userSavedJobRepository.findByUserIdAndJobId(user.getId(), job.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_SAVED));

        userSavedJobRepository.delete(userHasJob);
    }

    public void updateUserSavedJob(UserSavedJob userSavedJob, EntityStatus status){
        userSavedJob.setState(status);
        userSavedJobRepository.save(userSavedJob);
    }

    public List<UserSavedJob> getUserSavedJobByJobAndState(Long jobId, EntityStatus status){
        return userSavedJobRepository.findByJobIdAndState(jobId, status.name());
    }

    public List<UserSavedJob> getUserSavedJobByUserAndState(Long userId, EntityStatus status){
        return userSavedJobRepository.findByUserIdAndState(userId, status.name());
    }
}
