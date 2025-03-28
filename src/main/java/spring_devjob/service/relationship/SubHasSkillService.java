package spring_devjob.service.relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.entity.relationship.SubHasSkill;
import spring_devjob.repository.relationship.SubHasSkillRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubHasSkillService {

    private final SubHasSkillRepository subHasSkillRepository;

    public void deleteSubHasSkillBySub(long subId){
        List<SubHasSkill> subHasSkillList =  subHasSkillRepository.findBySubscriberId(subId);
        subHasSkillRepository.deleteAll(subHasSkillList);
    }

    public void deleteSubHasSkillBySkill(long skillId){
        List<SubHasSkill> subHasSkillList = subHasSkillRepository.findBySkillId(skillId);
        subHasSkillRepository.deleteAll(subHasSkillList);
    }

}
