package spring_devjob.repository.relationship;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.relationship.JobHasResume;
import spring_devjob.entity.relationship.UserSavedJob;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedJobRepository extends JpaRepository<UserSavedJob,Long> {

    Page<UserSavedJob> findAllByUserId(Pageable pageable, @Param("userId") Long userId);

    Optional<UserSavedJob> findByUserIdAndJobId(Long userId, Long jobId);

    @Query(value = "SELECT * FROM tbl_user_saved_job WHERE job_id = :jobId AND state = :state",
            nativeQuery = true)
    List<UserSavedJob> findByJobIdAndState(@Param("jobId") Long jobId,
                                           @Param("state") String state);

    @Query(value = "SELECT * FROM tbl_user_saved_job WHERE user_id = :userId AND state = :state",
            nativeQuery = true)
    List<UserSavedJob> findByUserIdAndState(@Param("userId") Long userId,
                                            @Param("state") String state);


}
