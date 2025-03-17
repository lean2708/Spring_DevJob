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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface ResumeRepository extends JpaRepository<Resume,Long> {

    @Modifying
    @Query(value = "UPDATE tbl_resume r " +
            "SET r.state = :state, r.deactivated_at = :deactivatedAt " +
            "WHERE r.user_id = :userId", nativeQuery = true)
    int updateAllResumesToInactiveByUserId(@Param("userId") Long userId,
                                           @Param("state") EntityStatus state,
                                           @Param("deactivatedAt") LocalDate deactivatedAt);


    Set<Resume> findAllByIdIn(Set<Long> ids);

    Page<Resume> findAllByUser(User user, Pageable pageable);

    Set<Resume> findAllByUserId(long userId);

    Page<Resume> findAllByJob(Job job, Pageable pageable);

    @Modifying
    @Query(value = "SELECT * FROM tbl_resume r WHERE r.state = :state AND r.deactivated_at < :date", nativeQuery = true)
    List<Resume> findInactiveResumesBeforeDate(@Param("state") EntityStatus state, @Param("date") LocalDate date);
}
