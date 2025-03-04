package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import spring_devjob.dto.basic.JobBasic;
import spring_devjob.dto.basic.UserBasic;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.JobMapper;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserMapper userMapper;
    private final JobMapper jobMapper;
    private final PageableService pageableService;
    private final AuthService authService;

    public ResumeResponse create(ResumeRequest request){
        if(resumeRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.RESUME_EXISTED);
        }
        Resume resume = resumeMapper.toResume(request);

        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resume.setUser(user);

        if(request.getJobId() != null) {
            Job job = jobRepository.findById(request.getJobId()).orElseThrow(
                    () -> new AppException(ErrorCode.JOB_NOT_EXISTED));
            resume.setJob(job);
        }

        return convertResumeResponse(resumeRepository.save(resume));
    }

    public ResumeResponse fetchResumeById(long id){
        Resume resumeDB = resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        return convertResumeResponse(resumeDB);
    }

    public PageResponse<ResumeResponse> getAllResumes(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Resume> resumePage = resumeRepository.findAll(pageable);

        List<ResumeResponse> responses =  convertListResumeResponse(resumePage.getContent());

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(responses)
                .build();
    }

    public ResumeResponse update(long id, ResumeRequest request){
        Resume resumeDB = resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        resumeMapper.updateResume(resumeDB, request);

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resumeDB.setUser(user);

        if(request.getJobId() != null) {
            Job job = jobRepository.findById(request.getJobId()).orElseThrow(
                    () -> new AppException(ErrorCode.JOB_NOT_EXISTED));
            resumeDB.setJob(job);
        }

        return convertResumeResponse(resumeRepository.save(resumeDB));
    }

    public void delete(long id){
        Resume resumeDB = resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        resumeRepository.delete(resumeDB);
    }

    public void deleteResumes(List<Long> ids){
        List<Resume> resumeList = resumeRepository.findAllByIdIn(ids);
        if(resumeList.isEmpty()){
            throw new AppException(ErrorCode.RESUME_NOT_FOUND);
        }
        resumeRepository.deleteAllInBatch(resumeList);
    }

    public PageResponse<ResumeResponse> getAllResumesByUser(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<Resume> resumePage = resumeRepository.findAllByUser(user, pageable);

        List<ResumeResponse> responses =  convertListResumeResponse(resumePage.getContent());

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)  // Thêm 1 để bắt đầu từ trang 1
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(responses)
                .build();
    }

    public PageResponse<ResumeResponse> getResumesByJob(int pageNo, int pageSize, String sortBy, long jobId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));

        Page<Resume> resumePage = resumeRepository.findAllByJob(job, pageable);

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)  // Thêm 1 để bắt đầu từ trang 1
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(convertListResumeResponse(resumePage.getContent()))
                .build();
    }

    public List<ResumeResponse> convertListResumeResponse(List<Resume> resumeList){
        List<ResumeResponse> resumeResponseList = new ArrayList<>();
        for(Resume resume : resumeList){
            ResumeResponse response = convertResumeResponse(resume);
            resumeResponseList.add(response);
        }
        return resumeResponseList;
    }

    public ResumeResponse convertResumeResponse(Resume resume){
        ResumeResponse response = resumeMapper.toResumeResponse(resume);

        UserBasic userBasic = (resume.getUser() != null) ?
                userMapper.toUserBasic(resume.getUser()) : null;

        response.setUser(userBasic);

        JobBasic jobBasic = (resume.getJob() != null) ?
                jobMapper.toJobBasic(resume.getJob()) : null;

        response.setJob(jobBasic);

        return response;
    }
}
