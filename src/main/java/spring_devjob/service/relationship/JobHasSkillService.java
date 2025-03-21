package spring_devjob.service.relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.repository.relationship.JobHasSkillRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHasSkillService {

    private final JobHasSkillRepository jobHasSkillRepository;

    public void updateJobHasSkillToInactive(JobHasSkill JobHasSkill){
        JobHasSkill.setState(EntityStatus.INACTIVE);
        jobHasSkillRepository.save(JobHasSkill);
    }

    public void updateJobHasSkillToActive(JobHasSkill JobHasSkill){
        JobHasSkill.setState(EntityStatus.ACTIVE);
        jobHasSkillRepository.save(JobHasSkill);
    }

}
