package spring_devjob.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.EntityStatus;
import spring_devjob.entity.Company;
import spring_devjob.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);

    Set<User> findAllByIdIn(Set<Long> ids);

    @Modifying
    @Query(value = "SELECT * FROM tbl_user u WHERE u.state = :state AND u.deactivated_at < :date", nativeQuery = true)
    List<User> findInactiveUsersBeforeDate(@Param("state") EntityStatus state, @Param("date") LocalDate date);
}
