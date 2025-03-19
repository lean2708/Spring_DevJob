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
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.JobHasResume;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long> {

    Set<Resume> findAllByIdIn(Set<Long> ids);

    Page<Resume> findAllByUser(User user, Pageable pageable);

    Set<Resume> findAllByUserId(long userId);

    @Query("SELECT r FROM Resume r " +
            "JOIN JobHasResume jhr ON r.id = jhr.resume.id " +
            "WHERE jhr.job = :job")
    Page<Resume> findAllByJob(@Param("job") Job job, Pageable pageable);

    @Query(value = "SELECT * FROM tbl_resume r WHERE r.id = :id", nativeQuery = true)
    Optional<Resume> findResumeById(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE tbl_resume r " +
            "SET r.state = :state, r.deactivated_at = :deactivatedAt " +
            "WHERE r.user_id = :userId", nativeQuery = true)
    int updateAllResumesByUserId(@Param("userId") Long userId,
                                           @Param("state") String state,
                                           @Param("deactivatedAt") LocalDate deactivatedAt);

    @Modifying
    @Query(value = "SELECT * FROM tbl_resume r WHERE r.state = :state AND r.deactivated_at < :date", nativeQuery = true)
    List<Resume> findInactiveResumesBeforeDate(@Param("state") String state, @Param("date") LocalDate date);
}
