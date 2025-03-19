package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.CompanyResponse;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.history.ResumeHistoryRepository;
import spring_devjob.service.relationship.JobHasResumeService;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final UserRepository userRepository;
    private final PageableService pageableService;
    private final AuthService authService;
    private final JobHasResumeService jobHasResumeService;
    private final ResumeHistoryRepository resumeHistoryRepository;


    public ResumeResponse create(ResumeRequest request){
        Resume resume = resumeMapper.toResume(request);

        User user = userRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resume.setUser(user);

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

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(resumeMapper.toResumeResponseList(resumePage.getContent()))
                .build();
    }

    public ResumeResponse update(long id, ResumeRequest request){
        Resume resumeDB = findActiveResumeById(id);

        resumeMapper.updateResume(resumeDB, request);

        return resumeMapper.toResumeResponse(resumeRepository.save(resumeDB));
    }

    @Transactional
    public void delete(long id){
        Resume resumeDB = findActiveResumeById(id);

        deactivateResume(resumeDB);

        resumeRepository.save(resumeDB);
    }

    private void deactivateResume(Resume resume){
        resume.getJobs().forEach(jobHasResumeService::updateJobHasResumeToInactive);

        resume.setState(EntityStatus.INACTIVE);
        resume.setDeactivatedAt(LocalDate.now());
    }

    @Transactional
    public void deleteResumes(Set<Long> ids){
        Set<Resume> resumeSet = resumeRepository.findAllByIdIn(ids);
        if(resumeSet.isEmpty()){
            throw new AppException(ErrorCode.RESUME_NOT_FOUND);
        }
        resumeSet.forEach(this::deactivateResume);

        resumeRepository.saveAll(resumeSet);
    }

    @Transactional
    public ResumeResponse restoreResume(long id) {
        if(resumeHistoryRepository.existsById(id)){
            throw new AppException(ErrorCode.RESUME_ARCHIVED_IN_HISTORY);
        }
        Resume resume = resumeRepository.findResumeById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));

        if (resume.getState() == EntityStatus.INACTIVE) {
            resume.getJobs().forEach(jobHasResumeService::updateJobHasResumeToActive);

            resume.setState(EntityStatus.ACTIVE);
            resume.setDeactivatedAt(null);
            return resumeMapper.toResumeResponse(resumeRepository.save(resume));
        }else {
            throw new AppException(ErrorCode.RESUME_ALREADY_ACTIVE);
        }
    }

    private Resume findActiveResumeById(long id) {
        return resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
    }

}
