package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;

import java.util.List;

@Repository
public interface JobHasSkillRepository extends JpaRepository<JobHasSkill,Long> {

    void deleteByJob(Job job);

    @Query("SELECT jhs FROM JobHasSkill jhs WHERE jhs.skill.id = :skillId")
    List<JobHasSkill> findBySkillId(@Param("skillId") Long skillId);

    @Query(value = "SELECT * FROM tbl_job_has_skill WHERE job_id = :jobId AND state = :state",
            nativeQuery = true)
    List<JobHasSkill> findByJobIdAndState(@Param("jobId") Long jobId,
                                          @Param("state") String state);


}
