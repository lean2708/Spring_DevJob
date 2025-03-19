package spring_devjob.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Company;
import spring_devjob.entity.Job;
import spring_devjob.entity.Resume;
import spring_devjob.entity.Skill;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {

    Page<Job> findAllByCompanyId(Long companyId, Pageable pageable);

    Set<Job> findBySkillsIn(Set<Skill> skills);

    Set<Job> findAllByIdIn(Set<Long> ids);

    @Query("SELECT COUNT(j) > 0 FROM Job j WHERE j.name = :name AND j.company.id = :companyId")
    boolean existsByNameAndCompanyId(@Param("name") String name, @Param("companyId") Long companyId);

    @Query(value = "SELECT * FROM tbl_job WHERE id = :id", nativeQuery = true)
    Optional<Job> findJobById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Job j SET j.jobStatus = false WHERE j.endDate < :currentDate AND j.jobStatus = true")
    int updateExpiredJobs(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT j FROM Job j " +
            "JOIN JobHasResume jhr ON j.id = jhr.job.id " +
            "WHERE jhr.resume IN :resumes")
    Page<Job> findAllByResumesIn(@Param("resumes") Set<Resume> resumes, Pageable pageable);


    @Modifying
    @Query(value = "SELECT * FROM tbl_job t WHERE t.state = :state AND t.deactivated_at < :date", nativeQuery = true)
    List<Job> findInactiveJobsBeforeDate(@Param("state") String state, @Param("date") LocalDate date);

    @Modifying
    @Query(value = "UPDATE tbl_job j " +
            "SET j.state = :state, j.deactivated_at = :deactivatedAt " +
            "WHERE j.company_id= :companyId", nativeQuery = true)
    int updateAllJobsByCompanyId(@Param("companyId") Long companyId,
                                           @Param("state") String state,
                                           @Param("deactivatedAt") LocalDate deactivatedAt);
}
