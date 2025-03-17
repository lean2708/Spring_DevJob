package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.relationship.JobHasSkill;

@Repository
public interface JobHasSkillRepository extends JpaRepository<JobHasSkill,Long> {
    void deleteByJob(Job job);
}
