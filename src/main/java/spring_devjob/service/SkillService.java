package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.Skill;
import spring_devjob.entity.Subscriber;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.SkillMapper;
import spring_devjob.repository.JobRepository;
import spring_devjob.repository.SkillRepository;
import spring_devjob.repository.SubscriberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final JobRepository jobRepository;
    private final SubscriberRepository subscriberRepository;
    private final PageableService pageableService;

    public SkillResponse create(SkillRequest request){
        if(skillRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }

        Skill skill = skillMapper.toSkill(request);

        return skillMapper.toSkillResponse(skillRepository.save(skill));
    }


    public SkillResponse fetchSkillById(long id){
        Skill skillDB = skillRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));

        return skillMapper.toSkillResponse(skillDB);
    }

    public PageResponse<SkillResponse> fetchAllSkills(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Skill> skillPage = skillRepository.findAll(pageable);

        List<SkillResponse> skillResponseList = new ArrayList<>();
        for(Skill skill : skillPage.getContent()){
            SkillResponse response = skillMapper.toSkillResponse(skill);
            skillResponseList.add(response);
        }

        return PageResponse.<SkillResponse>builder()
                .page(skillPage.getNumber() + 1)
                .size(skillPage.getSize())
                .totalPages(skillPage.getTotalPages())
                .totalItems(skillPage.getTotalElements())
                .items(skillResponseList)
                .build();
    }

    public SkillResponse update(long id, SkillRequest request){
        Skill skillDB = skillRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));

        skillMapper.updateSkill(skillDB, request);

        return skillMapper.toSkillResponse(skillRepository.save(skillDB));
    }

    @Transactional
    public void delete(long id){
        Skill skillDB = skillRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));

        processSkillDeletion(skillDB);

        skillRepository.delete(skillDB);
    }

    private void processSkillDeletion(Skill skill){
        if(!CollectionUtils.isEmpty(skill.getJobs())){
            skill.getJobs().clear();
        }

        if(!CollectionUtils.isEmpty(skill.getSubscribers())){
           skill.getSubscribers().clear();
        }
        skillRepository.save(skill);
    }

    @Transactional
    public void deleteSkills(Set<Long> ids){
        Set<Skill> skillSet = skillRepository.findAllByIdIn(ids);
        if(skillSet.isEmpty()){
            throw new AppException(ErrorCode.SKILL_NOT_FOUND);
        }

        skillSet.forEach(this::processSkillDeletion);

        skillRepository.deleteAllInBatch(skillSet);
    }
}
