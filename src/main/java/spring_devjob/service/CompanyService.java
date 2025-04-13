package spring_devjob.service;

import spring_devjob.dto.request.CompanyRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ReviewResponse;

import java.util.List;
import java.util.Set;

public interface CompanyService {

    CompanyResponse create(CompanyRequest request);

    CompanyResponse fetchCompany(long id);

    PageResponse<CompanyResponse> fetchAllCompanies(int pageNo, int pageSize, String sortBy);

    CompanyResponse update(long id, CompanyRequest request);

    void delete(long id);

    void deleteCompanies(Set<Long> ids);

    PageResponse<CompanyResponse> searchCompany(int pageNo, int pageSize, String sortBy, List<String> search);

    PageResponse<CompanyResponse> getTopRatedCompanies(int pageNo, int pageSize, double rating);

    PageResponse<JobResponse> getAllJobsByCompany(int pageNo, int pageSize, String sortBy, long companyId);

    PageResponse<ReviewResponse> getReviewsByCompany(int pageNo, int pageSize, String sortBy, long companyId);
}
