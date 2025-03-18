package spring_devjob.repository.relationship;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.relationship.UserSavedJob;

import java.util.Optional;

@Repository
public interface UserSavedJobRepository extends JpaRepository<UserSavedJob,Long> {

    Page<UserSavedJob> findAllByUserId(Pageable pageable, Long userId);

    Optional<UserSavedJob> findByUserIdAndJobId(Long userId, Long jobId);
}
