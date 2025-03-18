package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.request.UpdateCVStatusRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.JobService;
import spring_devjob.service.ResumeService;
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

    @PostMapping("/jobs")
    public ApiResponse<JobResponse> create(@Valid @RequestBody JobRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Job")
                .result(jobService.create(request))
                .build();
    }

    @GetMapping("/jobs/{id}")
    public ApiResponse<JobResponse> fetchJob(@Positive(message = "ID phải lớn hơn 0") @PathVariable long id){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Job By Id")
                .result(jobService.fetchJob(id))
                .build();
    }

    @GetMapping("/jobs")
    public ApiResponse<PageResponse<JobResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                               @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                           @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                               @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.fetchAllJobs(pageNo, pageSize, sortBy))
                .message("Fetch All Jobs With Pagination")
                .build();
    }

    @PutMapping("/jobs/{id}")
    public ApiResponse<JobResponse> update(@Positive(message = "ID phải lớn hơn 0")
                                               @PathVariable long id, @RequestBody JobRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update job By Id")
                .result(jobService.update(id, request))
                .build();
    }

    @DeleteMapping("/jobs/{id}")
    public ApiResponse<Void> delete(@Positive(message = "ID phải lớn hơn 0")
                                        @PathVariable long id){
        jobService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Job By Id")
                .result(null)
                .build();
    }

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

    @Operation(summary = "Fetch Jobs By Skills",
    description = "API này để lấy danh sách job theo field và skill name (với giá trị của search: field~value hoặc field>value hoặc field<value)")
    @GetMapping("/jobs/search-by-skills")
    public ApiResponse<PageResponse<JobResponse>> fetchAllBySkills(@RequestParam(defaultValue = "1") int pageNo,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                                   @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
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
    @GetMapping("/jobs/{jobId}/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> getResumesByJob(@RequestParam(defaultValue = "1") int pageNo,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                     @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                         @RequestParam(required = false) String sortBy,
                                                                     @Positive(message = "jobId phải lớn hơn 0")  @PathVariable(value = "jobId") long jobId){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.getResumesByJob(pageNo, pageSize, sortBy, jobId))
                .message("Get resumes for a specific job")
                .build();

    }

    @Operation(summary = "Resume applied to the job",
            description = "API này để nộp CV vào Job")
    @PostMapping("/jobs/{jobId}/resumes/{resumeId}")
    public ApiResponse<ApplyResponse> applyResume(@Positive(message = "jobId phải lớn hơn 0") @PathVariable(value = "jobId") Long jobId,
                                                  @Positive(message = "resumeId phải lớn hơn 0")  @PathVariable(value = "resumeId") Long resumeId){
        return ApiResponse.<ApplyResponse>builder()
                .code(HttpStatus.OK.value())
                .result(jobHasResumeService.applyResumeToJob(jobId, resumeId))
                .message("Resume applied successfully to the job")
                .build();

    }

    @Operation(summary = "Update CV status",
            description = "API này để cập nhật trạng thái của CV cho một công việc cụ thể (HR duyệt CV)")
    @PatchMapping("/jobs/{jobId}/resumes/{resumeId}/status")
    public ApiResponse<Void> updateCVStatus(@PathVariable Long jobId,
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
