package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.JobRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.service.JobService;
import spring_devjob.service.ResumeService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class JobController {
    private final JobService jobService;
    private final ResumeService resumeService;

    @PostMapping("/jobs")
    public ApiResponse<JobResponse> create(@Valid @RequestBody JobRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Job")
                .result(jobService.create(request))
                .build();
    }

    @GetMapping("/jobs/{id}")
    public ApiResponse<JobResponse> fetchJob(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable long id){
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
    public ApiResponse<JobResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                               @PathVariable long id, @RequestBody JobRequest request){
        return ApiResponse.<JobResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update job By Id")
                .result(jobService.update(id, request))
                .build();
    }

    @DeleteMapping("/jobs/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        jobService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Job By Id")
                .result(null)
                .build();
    }

    @DeleteMapping("/jobs")
    public ApiResponse<Void> deleteJobs(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                            List<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        jobService.deleteJobs(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Jobs")
                .result(null)
                .build();
    }

    @Operation(summary = "Fetch Jobs By Skills",
    description = "API này để lấy danh sách job theo field và skill name")
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


    @Operation(summary = "Get list of applied jobs",
               description = "API này để láy danh sách job mà người dùng đã ứng tuyển")
    @GetMapping("/applied-jobs-by-user")
    public ApiResponse<PageResponse<JobResponse>> getAllAppliedJobsByUser(@RequestParam(defaultValue = "1") int pageNo,
                                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                                     @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                         @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.getAllAppliedJobsByUser(pageNo, pageSize, sortBy))
                .message("Fetch All Jobs By User")
                .build();
    }

    @Operation(summary = "Get resumes for a specific job",
    description = "API này để lấy tất cả cv của một job")
    @GetMapping("/{jobId}/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> getResumesByJob(@RequestParam(defaultValue = "1") int pageNo,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                     @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                         @RequestParam(required = false) String sortBy,
                                                                     @PathVariable("jobId") long jobId){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(resumeService.getResumesByJob(pageNo, pageSize, sortBy, jobId))
                .message("Get resumes for a specific job")
                .build();

    }
}
