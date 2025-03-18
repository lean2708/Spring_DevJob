package spring_devjob.service.relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.relationship.SubHasSkill;
import spring_devjob.repository.relationship.SubHasSkillRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubHasSkillService {

    private final SubHasSkillRepository subHasSkillRepository;

    public void updateJobHasSkillToInactive(SubHasSkill subHasSkill){
        subHasSkill.setState(EntityStatus.INACTIVE);
        subHasSkillRepository.save(subHasSkill);
    }
}
