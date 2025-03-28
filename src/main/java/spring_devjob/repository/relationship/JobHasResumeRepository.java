package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.relationship.JobHasResume;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobHasResumeRepository  extends JpaRepository<JobHasResume,Long> {

    Optional<JobHasResume> findByJobAndResume(Job job, Resume resume);

    @Query(value = "SELECT * FROM tbl_job_has_resume WHERE job_id = :jobId AND state = :state",
            nativeQuery = true)
    List<JobHasResume> findByJobIdAndState(@Param("jobId") Long jobId,
                                           @Param("state") String state);

    @Query(value = "SELECT * FROM tbl_job_has_resume WHERE resume_id = :resumeId AND state = :state",
            nativeQuery = true)
    List<JobHasResume> findByResumeIdAndState(@Param("resumeId") Long resumeId,
                                              @Param("state") String state);

}
