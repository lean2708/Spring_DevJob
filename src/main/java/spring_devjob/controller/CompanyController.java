package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.ApiResponse;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.service.CompanyService;
import spring_devjob.service.JobService;

import java.security.PublicKey;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class CompanyController {
    private final CompanyService companyService;
    private final JobService jobService;

    @PostMapping("/company")
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Company")
                .result(companyService.create(request))
                .build();
    }

    @GetMapping("/company/{id}")
    public ApiResponse<CompanyResponse> fetchCompany(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                         @PathVariable("id") long id){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Company By Id")
                .result(companyService.fetchCompany(id))
                .build();
    }

    @GetMapping("/companies")
    public ApiResponse<PageResponse<CompanyResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                   @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                               @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                   @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<CompanyResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.fetchAllCompanies(pageNo, pageSize, sortBy))
                .message("Fetch All Company With Pagination")
                .build();
    }

    @PutMapping("/company/{id}")
    public ApiResponse<CompanyResponse> update(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1")
                                                   @PathVariable long id, @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Company By Id")
                .result(companyService.update(id, request))
                .build();
    }

    @DeleteMapping("/company/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "ID phải lớn hơn hoặc bằng 1") @PathVariable long id){
        companyService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Delete Company By Id")
                .result(null)
                .build();
    }
    @Operation(summary = "Search companies based on attributes with pagination")
    @GetMapping("/companies/search")
    public ApiResponse<PageResponse<CompanyResponse>> searchCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                    @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                        @RequestParam(required = false) String sortBy,
                                                                    @RequestParam(required = false) List<String> search){
        return ApiResponse.<PageResponse<CompanyResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.searchCompany(pageNo, pageSize, sortBy, search))
                .message("Search companies based on attributes with pagination")
                .build();
    }
    @Operation(summary = "Get a paginated list of jobs for a company",
            description = "API này để lấy danh sách công việc của một công ty")
    @GetMapping("/company/all-jobs")
    public  ApiResponse<PageResponse<JobResponse>> getAllJobsByCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                       @RequestParam(defaultValue = "1") int pageNo,
                                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                                       @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                       @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(jobService.getAllJobsByCompany(pageNo, pageSize, sortBy))
                .message("Get a paginated list of jobs for a company")
                .build();
    }
}
