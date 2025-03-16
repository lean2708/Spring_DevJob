package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PageableService pageableService;
    private final AuthService authService;

    public ResumeResponse create(ResumeRequest request){
        if(resumeRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.RESUME_EXISTED);
        }
        if(resumeRepository.existsInactiveResumeByName(request.getName())){
            throw new AppException(ErrorCode.RESUME_ALREADY_DELETED);
        }
        Resume resume = resumeMapper.toResume(request);

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resume.setUser(user);

        if(request.getJobId() != null) {
            jobRepository.findById(request.getJobId()).ifPresent(resume::setJob);
        }

        return resumeMapper.toResumeResponse(resumeRepository.save(resume));
    }

    public ResumeResponse fetchResumeById(long id){
        Resume resumeDB = findActiveResumeById(id);

        return resumeMapper.toResumeResponse(resumeDB);
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
        Resume resumeDB = findActiveResumeById(id);

        resumeMapper.updateResume(resumeDB, request);

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resumeDB.setUser(user);

        if(request.getJobId() != null) {
            jobRepository.findById(request.getJobId()).ifPresent(resumeDB::setJob);
        }

        return resumeMapper.toResumeResponse(resumeRepository.save(resumeDB));
    }

    @Transactional
    public void delete(long id){
        Resume resumeDB = findActiveResumeById(id);

        resumeDB.setState(EntityStatus.INACTIVE);
        resumeDB.setDeactivatedAt(LocalDate.now());

        resumeRepository.delete(resumeDB);
    }

    @Transactional
    public void deleteResumes(Set<Long> ids){
        Set<Resume> resumeSet = resumeRepository.findAllByIdIn(ids);
        if(resumeSet.isEmpty()){
            throw new AppException(ErrorCode.RESUME_NOT_FOUND);
        }
        resumeSet.forEach(resume -> {
            resume.setState(EntityStatus.INACTIVE);
            resume.setDeactivatedAt(LocalDate.now());
        });
        resumeRepository.saveAll(resumeSet);
    }

    public PageResponse<ResumeResponse> getAllResumesByUser(int pageNo, int pageSize, String sortBy, long userId){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

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

    private Resume findActiveResumeById(long id) {
        return resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
    }

    public List<ResumeResponse> convertListResumeResponse(List<Resume> resumeList){
        List<ResumeResponse> resumeResponseList = new ArrayList<>();
        for(Resume resume : resumeList){
            ResumeResponse response = resumeMapper.toResumeResponse(resume);
            resumeResponseList.add(response);
        }
        return resumeResponseList;
    }

}
