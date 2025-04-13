package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.CompanyService;
import spring_devjob.service.RestoreService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1")
@RestController
public class CompanyController {

    private final CompanyService companyService;
    private final RestoreService restoreService;

    @PreAuthorize("hasAuthority('CREATE_COMPANY')")
    @PostMapping("/companies")
    public ApiResponse<CompanyResponse> create(@Valid @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Create Company")
                .result(companyService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_COMPANY_BY_ID')")
    @GetMapping("/companies/{id}")
    public ApiResponse<CompanyResponse> fetchCompany(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                         @PathVariable("id") long id){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Company By Id")
                .result(companyService.fetchCompany(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_COMPANIES')")
    @GetMapping("/companies")
    public ApiResponse<PageResponse<CompanyResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                   @RequestParam(defaultValue = "1") int pageNo,
                                                               @RequestParam(defaultValue = "10") int pageSize,
                                                                   @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<CompanyResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.fetchAllCompanies(pageNo, pageSize, sortBy))
                .message("Fetch All Company With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_COMPANY')")
    @PutMapping("/companies/{id}")
    public ApiResponse<CompanyResponse> update(@Min(value = 1, message = "Id phải lớn hơn 0")
                                                   @PathVariable long id, @RequestBody CompanyRequest request){
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Update Company By Id")
                .result(companyService.update(id, request))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_COMPANY')")
    @DeleteMapping("/companies/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "Id phải lớn hơn 0") @PathVariable long id){
        companyService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Company By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_MULTIPLE_COMPANIES')")
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
    @PreAuthorize("hasAuthority('RESTORE_COMPANY')")
    @PatchMapping("/companies/{id}/restore")
    public ApiResponse<CompanyResponse> restore(@Min(value = 1, message = "Id phải lớn hơn 0")
                                             @PathVariable long id) {
        return ApiResponse.<CompanyResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Restore User By Id")
                .result(restoreService.restoreCompany(id))
                .build();
    }

    @Operation(summary = "Search companies based on attributes with pagination",
    description = "Giá trị của search: field~value hoặc field>value hoặc field<value")
    @PreAuthorize("hasAuthority('SEARCH_COMPANIES')")
    @GetMapping("/companies/search")
    public ApiResponse<PageResponse<CompanyResponse>> searchCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                        @RequestParam(defaultValue = "1") int pageNo,
                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                    @RequestParam(required = false) String sortBy,
                                                                    @RequestParam(required = false) List<String> search){
        return ApiResponse.<PageResponse<CompanyResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.searchCompany(pageNo, pageSize, sortBy, search))
                .message("Search companies based on attributes with pagination")
                .build();
    }

    @Operation(summary = "Fetch top-rated companies",
              description = "Lọc danh sách công ty có rating >= x, sắp xếp giảm dần theo rating.")
    @PreAuthorize("hasAuthority('FETCH_TOP_RATED_COMPANIES')")
    @GetMapping("/companies/top-rated")
    public ApiResponse<PageResponse<CompanyResponse>> getTopRatedCompanies(@RequestParam(defaultValue = "1") int pageNo,
                                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                                           @Min(4) @Max(5)
                                                                           @RequestParam(defaultValue = "4.5") double rating) {
        return ApiResponse.<PageResponse<CompanyResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.getTopRatedCompanies(pageNo, pageSize, rating))
                .message("Fetched top-rated companies")
                .build();
    }


    @Operation(summary = "Get a paginated list of jobs for a company",
            description = "API này để lấy danh sách công việc của một công ty")
    @PreAuthorize("hasAuthority('FETCH_JOBS_BY_COMPANY')")
    @GetMapping("/companies/{companyId}/all-jobs")
    public  ApiResponse<PageResponse<JobResponse>> getAllJobsByCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                       @RequestParam(defaultValue = "1") int pageNo,
                                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                                       @RequestParam(required = false) String sortBy,
                                                                       @Min(value = 1, message = "companyId phải lớn hơn 0") @PathVariable(value = "companyId") long companyId){
        return ApiResponse.<PageResponse<JobResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.getAllJobsByCompany(pageNo, pageSize, sortBy, companyId))
                .message("Get a paginated list of jobs for a company")
                .build();
    }

    @Operation(description = "API này để lấy các review của một công ty")
    @PreAuthorize("hasAuthority('FETCH_REVIEWS_BY_COMPANY')")
    @GetMapping("/companies/{companyId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getReviewsByCompany(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                              @RequestParam(defaultValue = "1") int pageNo,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) String sortBy,
                                                                         @Min(value = 1, message = "companyId phải lớn hơn 0")
                                                                         @PathVariable(value = "companyId") long companyId){
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(companyService.getReviewsByCompany(pageNo, pageSize, sortBy, companyId))
                .message("Retrieved company reviews with pagination")
                .build();
    }
}
