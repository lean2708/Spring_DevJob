package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.JobHasSkill;

import java.util.Optional;

@Repository
public interface JobHasResumeRepository  extends JpaRepository<JobHasResume,Long> {
    Optional<JobHasResume> findByJobAndResume(Job job, Resume resume);
}
