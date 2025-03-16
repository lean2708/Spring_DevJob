package spring_devjob.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.service.ResumeService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class ResumeController {
    private final ResumeService resumeService;

    @PostMapping("/resumes")
    public ApiResponse<ResumeResponse> create(@Valid @RequestBody ResumeRequest request){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Resume")
                .result(resumeService.create(request))
                .build();
    }

    @GetMapping("/resumes/{id}")
    public ApiResponse<ResumeResponse> fetchResume(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                       @PathVariable long id){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Resume By Id")
                .result(resumeService.fetchResumeById(id))
                .build();
    }

    @GetMapping("/resumes")
    public ApiResponse<PageResponse<ResumeResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                  @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                              @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                  @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<ResumeResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(resumeService.getAllResumes(pageNo, pageSize, sortBy))
                .message("Fetch All Resumes With Pagination")
                .build();
    }

    @PutMapping("/resumes/{id}")
    public ApiResponse<ResumeResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                  @PathVariable long id, @RequestBody ResumeRequest request){
        return ApiResponse.<ResumeResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Resume By Id")
                .result(resumeService.update(id, request))
                .build();
    }

    @DeleteMapping("/resumes/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                        @PathVariable long id){
        resumeService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Resume By Id")
                .result(null)
                .build();
    }
    @DeleteMapping("/resumes")
    public ApiResponse<Void> deleteResumes(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                           Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        resumeService.deleteResumes(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Resumes")
                .result(null)
                .build();
    }

}
