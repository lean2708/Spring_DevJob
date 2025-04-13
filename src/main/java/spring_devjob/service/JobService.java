package spring_devjob.service;

import spring_devjob.dto.request.JobCreationRequest;
import spring_devjob.dto.request.JobUpdateRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;

import java.util.List;
import java.util.Set;

public interface JobService {

    JobResponse create(JobCreationRequest request);

    JobResponse fetchJob(long id);

    PageResponse<JobResponse> fetchAllJobs(int pageNo, int pageSize, String sortBy);

    JobResponse update(long id, JobUpdateRequest request);

    void delete(long id);

    void deleteJobs(Set<Long> ids);

    PageResponse<JobResponse> fetchAllJobsBySkills(int pageNo, int pageSize, String sortBy, List<String> search, List<String> skills);

    PageResponse<ResumeResponse> getResumesByJob(int pageNo, int pageSize, String sortBy, long jobId);

}
