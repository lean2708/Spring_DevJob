package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.service.RestoreService;
import spring_devjob.service.ResumeService;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class ResumeController {
    private final ResumeService resumeService;
    private final RestoreService restoreService;

    @PreAuthorize("hasAuthority('CREATE_RESUME')")
    @PostMapping("/resumes")
    public ApiResponse<ResumeResponse> create(@Valid @RequestBody ResumeRequest request){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Resume")
                .result(resumeService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_RESUME_BY_ID')")
    @GetMapping("/resumes/{id}")
    public ApiResponse<ResumeResponse> fetchResume(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                       @PathVariable long id){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Resume By Id")
                .result(resumeService.fetchResumeById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_RESUMES')")
    @GetMapping("/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                  @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                                  @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(resumeService.getAllResumes(pageNo, pageSize, sortBy))
                .message("Fetch All Resumes With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_RESUME')")
    @PutMapping("/resumes/{id}")
    public ApiResponse<ResumeResponse> update(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                  @PathVariable long id, @RequestBody ResumeRequest request){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Resume By Id")
                .result(resumeService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_RESUME')")
    @DeleteMapping("/resumes/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "Id phải lớn hơn 0")
                                        @PathVariable long id){
        resumeService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Resume By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_MULTIPLE_RESUMES')")
    @DeleteMapping("/resumes")
    public ApiResponse<Void> deleteResumes(@RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                           Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        resumeService.deleteResumes(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Resumes")
                .result(null)
                .build();
    }

    @Operation(summary = "Restore a deleted resume",
            description = "API này để khôi phục resume đã xóa trước đó")
    @PreAuthorize("hasAuthority('RESTORE_RESUME')")
    @PatchMapping("/resumes/{id}/restore")
    public ApiResponse<ResumeResponse> restoreResume(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                     @PathVariable long id) {
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Restore Resume By Id")
                .result(restoreService.restoreResume(id))
                .build();
    }

}
