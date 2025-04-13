package spring_devjob.service;

import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;

import java.util.Set;

public interface ResumeService {

    ResumeResponse create(ResumeRequest request);

    ResumeResponse fetchResumeById(long id);

    PageResponse<ResumeResponse> getAllResumes(int pageNo, int pageSize, String sortBy);

    ResumeResponse update(long id, ResumeRequest request);

    void delete(long id);

    void deleteResumes(Set<Long> ids);
}
