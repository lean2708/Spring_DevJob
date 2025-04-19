package spring_devjob.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.dto.request.ResumeRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.ResumeResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.ResumeMapper;
import spring_devjob.repository.ResumeRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.service.*;

import java.util.Set;

@Slf4j(topic = "RESUME-SERVICE")
@RequiredArgsConstructor
@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeMapper resumeMapper;
    private final UserRepository userRepository;
    private final PageableService pageableService;
    private final CurrentUserService currentUserService;
    private final EntityDeactivationService entityDeactivationService;


    @Override
    public ResumeResponse create(ResumeRequest request){
        Resume resume = resumeMapper.toResume(request);

        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        resume.setUser(user);

        return resumeMapper.toResumeResponse(resumeRepository.save(resume));
    }

    @Override
    public ResumeResponse fetchResumeById(long id){
        Resume resumeDB = findActiveResumeById(id);

        return resumeMapper.toResumeResponse(resumeDB);
    }

    @Override
    public PageResponse<ResumeResponse> getAllResumes(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy, Resume.class);

        Page<Resume> resumePage = resumeRepository.findAll(pageable);

        return PageResponse.<ResumeResponse>builder()
                .page(resumePage.getNumber() + 1)
                .size(resumePage.getSize())
                .totalPages(resumePage.getTotalPages())
                .totalItems(resumePage.getTotalElements())
                .items(resumeMapper.toResumeResponseList(resumePage.getContent()))
                .build();
    }

    @Override
    public ResumeResponse update(long id, ResumeRequest request){
        Resume resumeDB = findActiveResumeById(id);

        resumeMapper.updateResume(resumeDB, request);

        return resumeMapper.toResumeResponse(resumeRepository.save(resumeDB));
    }

    @Transactional
    @Override
    public void delete(long id){
        Resume resumeDB = findActiveResumeById(id);

        entityDeactivationService.deactivateResume(resumeDB);
    }

    @Transactional
    @Override
    public void deleteResumes(Set<Long> ids){
        Set<Resume> resumeSet = resumeRepository.findAllByIdIn(ids);
        if(resumeSet.isEmpty()){
            throw new AppException(ErrorCode.RESUME_NOT_FOUND);
        }
        resumeSet.forEach(entityDeactivationService::deactivateResume);
    }


    private Resume findActiveResumeById(long id) {
        return resumeRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
    }

}
