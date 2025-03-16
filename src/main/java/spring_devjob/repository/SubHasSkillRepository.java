package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.JobHasSkill;
import spring_devjob.entity.SubHasSkill;

@Repository
public interface SubHasSkillRepository extends JpaRepository<SubHasSkill,Long> {
}
