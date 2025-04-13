package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.service.relationship.SavedJobService;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class SavedJobController {

    private final SavedJobService savedJobService;

    @Operation(summary = "Save a job",
            description = "API này cho phép user lưu job")
    @PreAuthorize("hasAuthority('SAVE_JOB')")
    @PostMapping("/saved-jobs/{jobId}")
    public ApiResponse<Void> saveJob(@Min(value = 1, message = "jobId phải lớn hơn 0") @PathVariable Long jobId) {
        savedJobService.saveJob(jobId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Saved Job for a User")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('GET_SAVED_JOBS')")
    @Operation(summary = "Fetched all saved jobs By User")
    @GetMapping("/saved-jobs")
    public ApiResponse<PageResponse<JobResponse>> getSavedJobs(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                           @RequestParam(defaultValue = "1") int pageNo,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(savedJobService.getSavedJobs(pageNo, pageSize, sortBy))
                .message("Fetched all saved jobs By User")
                .build();
    }

    @PreAuthorize("hasAuthority('REMOVE_SAVED_JOB')")
    @Operation(summary = "Remove a saved Job for a User")
    @DeleteMapping("/saved-jobs/{jobId}")
    public ApiResponse<Void> removeSaveJob(@Min(value = 1, message = "jobId phải lớn hơn 0") @PathVariable long jobId) {
        savedJobService.removeSaveJob(jobId);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Remove a saved Job for a User")
                .result(null)
                .build();
    }
}
