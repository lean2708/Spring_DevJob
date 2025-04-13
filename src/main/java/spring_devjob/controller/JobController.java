package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.JobCreationRequest;
import spring_devjob.dto.request.JobUpdateRequest;
import spring_devjob.dto.request.UpdateCVStatusRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.JobService;
import spring_devjob.service.RestoreService;
import spring_devjob.service.relationship.JobHasResumeService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class JobController {
    private final JobService jobService;
    private final JobHasResumeService jobHasResumeService;
    private final RestoreService restoreService;

    @PreAuthorize("hasAuthority('CREATE_JOB')")
    @PostMapping("/jobs")
    public ApiResponse<JobResponse> create(@Valid @RequestBody JobCreationRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Job")
                .result(jobService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_JOB_BY_ID')")
    @GetMapping("/jobs/{id}")
    public ApiResponse<JobResponse> fetchJob(@Min(value = 1, message = "Id phải lớn hơn 0") @PathVariable long id){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Job By Id")
                .result(jobService.fetchJob(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_JOBS')")
    @GetMapping("/jobs")
    public ApiResponse<PageResponse<JobResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                               @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.fetchAllJobs(pageNo, pageSize, sortBy))
                .message("Fetch All Jobs With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_JOB')")
    @PutMapping("/jobs/{id}")
    public ApiResponse<JobResponse> update(@Min(value = 1, message = "Id phải lớn hơn 0")
                                               @PathVariable long id, @RequestBody JobUpdateRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update job By Id")
                .result(jobService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_JOB')")
    @DeleteMapping("/jobs/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "Id phải lớn hơn 0")
                                        @PathVariable long id){
        jobService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Job By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_MULTIPLE_JOBS')")
    @DeleteMapping("/jobs")
    public ApiResponse<Void> deleteJobs(@RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                        Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        jobService.deleteJobs(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Jobs")
                .result(null)
                .build();
    }

    @Operation(summary = "Restore Job",
            description = "API này để khôi phục trạng thái của Job từ INACTIVE về ACTIVE")
    @PreAuthorize("hasAuthority('RESTORE_JOB')")
    @PatchMapping("/jobs/{id}/restore")
    public ApiResponse<JobResponse> restoreJob(@Min(value = 1, message = "Id phải lớn hơn 0") @PathVariable long id) {
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Khôi phục Job thành công")
                .result(restoreService.restoreJob(id))
                .build();
    }

    @Operation(summary = "Fetch Jobs By Skills",
    description = "API này để lấy danh sách job theo field và skill name (với giá trị của search: field~value hoặc field>value hoặc field<value)")
    @PreAuthorize("hasAuthority('SEARCH_JOBS_BY_SKILLS')")
    @GetMapping("/jobs/search-by-skills")
    public ApiResponse<PageResponse<JobResponse>> fetchAllBySkills(@RequestParam(defaultValue = "1") int pageNo,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                                       @RequestParam(required = false) String sortBy,
                                                           @RequestParam(required = false) List<String> search,
                                                           @RequestParam(required = false) List<String> skills){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.fetchAllJobsBySkills(pageNo, pageSize, sortBy, search, skills))
                .message("Fetch All Jobs by Skills")
                .build();
    }

    @Operation(summary = "Get resumes for a specific job",
    description = "API này để lấy tất cả cv của một job")
    @PreAuthorize("hasAuthority('FETCH_RESUMES_BY_JOB')")
    @GetMapping("/jobs/{jobId}/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> getResumesByJob(@RequestParam(defaultValue = "1") int pageNo,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                         @RequestParam(required = false) String sortBy,
                                                                     @Min(value = 1, message = "jobId phải lớn hơn 0")  @PathVariable(value = "jobId") long jobId){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.getResumesByJob(pageNo, pageSize, sortBy, jobId))
                .message("Get resumes for a specific job")
                .build();

    }

    @Operation(summary = "Resume applied to the job",
            description = "API này để nộp CV vào Job")
    @PreAuthorize("hasAuthority('APPLY_RESUME_TO_JOB')")
    @PostMapping("/jobs/{jobId}/applications/{resumeId}")
    public ApiResponse<ApplyResponse> applyResume(@Min(value = 1, message = "jobId phải lớn hơn 0") @PathVariable(value = "jobId") Long jobId,
                                                  @Min(value = 1, message = "resumeId phải lớn hơn 0") @PathVariable(value = "resumeId") Long resumeId){
        return ApiResponse.<ApplyResponse>builder()
                .code(HttpStatus.OK.value())
                .result(jobHasResumeService.applyResumeToJob(jobId, resumeId))
                .message("Resume applied successfully to the job")
                .build();

    }

    @Operation(summary = "Update CV status",
            description = "API này để cập nhật trạng thái của CV cho một công việc cụ thể (HR duyệt CV)")
    @PreAuthorize("hasAuthority('UPDATE_CV_STATUS')")
    @PatchMapping("/jobs/{jobId}/resumes/{resumeId}/status")
    public ApiResponse<Void> updateCVStatus(@Min(value = 1, message = "jobId phải lớn hơn 0")
                                                @PathVariable Long jobId,
                                            @Min(value = 1, message = "resumeId phải lớn hơn 0")
                                            @PathVariable Long resumeId,
                                            @Valid @RequestBody UpdateCVStatusRequest request) {
        jobHasResumeService.updateCVStatus(jobId, resumeId, request.getResumeStatus());
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái CV thành công")
                .result(null)
                .build();
    }

}
