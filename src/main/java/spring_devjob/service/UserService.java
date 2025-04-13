package spring_devjob.service;

import spring_devjob.dto.request.UserCreationRequest;
import spring_devjob.dto.request.UserUpdateRequest;
import spring_devjob.dto.response.JobResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.dto.response.UserResponse;

import java.util.Set;

public interface UserService {

    UserResponse create(UserCreationRequest request);

    UserResponse fetchUserById(Long id);

    PageResponse<UserResponse> fetchAllUsers(int pageNo, int pageSize, String sortBy);

    UserResponse update(long id, UserUpdateRequest request);

    void delete(long id);

    void deleteUsers(Set<Long> ids);

    PageResponse<ResumeResponse> getAllResumesByUser(int pageNo, int pageSize, String sortBy, long userId);

    PageResponse<JobResponse> getAllAppliedJobsByUser(int pageNo, int pageSize, String sortBy, long userId);
}
