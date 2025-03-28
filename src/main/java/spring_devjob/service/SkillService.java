package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import spring_devjob.dto.request.SkillRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.SkillResponse;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.SkillMapper;
import spring_devjob.repository.SkillRepository;
import spring_devjob.service.relationship.JobHasSkillService;
import spring_devjob.service.relationship.SubHasSkillService;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final PageableService pageableService;
    private final SubHasSkillService subHasSkillService;
    private final JobHasSkillService jobHasSkillService;

    public SkillResponse create(SkillRequest request){
        if(skillRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.SKILL_EXISTED);
        }
        Skill skill = skillMapper.toSkill(request);

        return skillMapper.toSkillResponse(skillRepository.save(skill));
    }


    public SkillResponse fetchSkillById(long id){
        Skill skillDB = findActiveSkillById(id);

        return skillMapper.toSkillResponse(skillDB);
    }

    public PageResponse<SkillResponse> fetchAllSkills(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Skill> skillPage = skillRepository.findAll(pageable);

        return PageResponse.<SkillResponse>builder()
                .page(skillPage.getNumber() + 1)
                .size(skillPage.getSize())
                .totalPages(skillPage.getTotalPages())
                .totalItems(skillPage.getTotalElements())
                .items(skillMapper.toSkillResponseList(skillPage.getContent()))
                .build();
    }

    public SkillResponse update(long id, SkillRequest request){
        Skill skillDB = findActiveSkillById(id);

        skillMapper.updateSkill(skillDB, request);

        return skillMapper.toSkillResponse(skillRepository.save(skillDB));
    }

    @Transactional
    public void delete(long id){
        Skill skillDB = findActiveSkillById(id);

        deactivateSkill(skillDB);

        skillRepository.delete(skillDB);
    }

    private void deactivateSkill(Skill skill){
        subHasSkillService.deleteSubHasSkillBySkill(skill.getId());

        jobHasSkillService.deleteJobHasSkillBySkill(skill.getId());
    }

    @Transactional
    public void deleteSkills(Set<Long> ids){
        Set<Skill> skillSet = skillRepository.findAllByIdIn(ids);
        if(skillSet.isEmpty()){
            throw new AppException(ErrorCode.SKILL_NOT_FOUND);
        }

        skillSet.forEach(this::deactivateSkill);

        skillRepository.deleteAllInBatch(skillSet);
    }

    private Skill findActiveSkillById(long id) {
        return skillRepository.findById(id).
                orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_EXISTED));
    }
}
