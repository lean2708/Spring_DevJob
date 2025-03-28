package spring_devjob.service.relationship;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.relationship.JobHasSkill;
import spring_devjob.repository.relationship.JobHasSkillRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobHasSkillService {

    private final JobHasSkillRepository jobHasSkillRepository;

    public void updateJobHasSkill(JobHasSkill JobHasSkill, EntityStatus status){
        JobHasSkill.setState(status);
        jobHasSkillRepository.save(JobHasSkill);
    }

    public List<JobHasSkill> getJobHasSkillByJobAndState(Long jobId, EntityStatus status){
        return jobHasSkillRepository.findByJobIdAndState(jobId, status.name());
    }

    public void deleteJobHasSkillBySkill(long skillId){
        List<JobHasSkill> jobHasSkillList = jobHasSkillRepository.findBySkillId(skillId);

        jobHasSkillRepository.deleteAll(jobHasSkillList);
    }

}
