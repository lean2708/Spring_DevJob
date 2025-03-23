package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.CompanyService;
import spring_devjob.service.JobService;
import spring_devjob.service.ReviewService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class CompanyController {

    private final CompanyService companyService;

    @PreAuthorize("hasAuthority('CREATE_COMPANY')")
    @PostMapping("/companies")
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Company")
                .result(companyService.create(request))
                .build();
    }

    @GetMapping("/companies/{id}")
    public ApiResponse<CompanyResponse> fetchCompany(@Positive(message = "CompanyID phải lớn hơn 0")
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

    @PutMapping("/companies/{id}")
    public ApiResponse<CompanyResponse> update(@Positive(message = "CompanyID phải lớn hơn 0")
                                                   @PathVariable long id, @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Company By Id")
                .result(companyService.update(id, request))
                .build();
    }

    @DeleteMapping("/companies/{id}")
    public ApiResponse<Void> delete(@Positive(message = "CompanyID phải lớn hơn 0") @PathVariable long id){
        companyService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Company By Id")
                .result(null)
                .build();
    }

    @DeleteMapping("/companies")
    public ApiResponse<Void> deleteCompanies(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                             Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        companyService.deleteCompanies(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Deleted Companies")
                .result(null)
                .build();
    }

    @Operation(summary = "Restore Company",
            description = "API này được sử dụng để phục hồi Company đã bị xóa mềm")
    @PatchMapping("/companies/{id}/restore")
    public ApiResponse<CompanyResponse> restore(@Positive(message = "ID phải lớn hơn 0")
                                             @PathVariable long id) {
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Restore User By Id")
                .result(companyService.restoreCompany(id))
                .build();
    }

    @Operation(summary = "Search companies based on attributes with pagination",
    description = "Giá trị của search: field~value hoặc field>value hoặc field<value")
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
    @GetMapping("/companies/{companyId}/all-jobs")
    public  ApiResponse<PageResponse<JobResponse>> getAllJobsByCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                       @RequestParam(defaultValue = "1") int pageNo,
                                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                                       @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                                       @RequestParam(required = false) String sortBy,
                                                                       @Positive(message = "companyId phải lớn hơn 0") @PathVariable(value = "companyId") long companyId){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.getAllJobsByCompany(pageNo, pageSize, sortBy, companyId))
                .message("Get a paginated list of jobs for a company")
                .build();
    }

    @Operation(description = "API này để lấy các review của một công ty")
    @GetMapping("/companies/{companyId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getReviewsByCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                              @RequestParam(defaultValue = "1") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @Pattern(regexp = "^(\\w+?)(-)(asc|desc)$", message = "Định dạng của sortBy phải là: field-asc hoặc field-desc")
                                                              @RequestParam(required = false) String sortBy,
                                                                         @Positive(message = "companyId phải lớn hơn 0")
                                                                         @PathVariable(value = "companyId") long companyId){
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.getReviewsByCompany(pageNo, pageSize, sortBy, companyId))
                .message("Retrieved company reviews with pagination")
                .build();
    }
}
