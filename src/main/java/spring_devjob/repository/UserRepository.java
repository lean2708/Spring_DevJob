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

    @Query(value = "SELECT * FROM tbl_user u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findUserById(@Param("id") Long id);
    @Query(value = "SELECT * FROM tbl_user u WHERE u.email= :email", nativeQuery = true)
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM tbl_user u WHERE u.state = :state AND u.email = :email", nativeQuery = true)
    long existsUserInactiveByEmail(@Param("email") String email, @Param("state") String state);

    @Query(value = "SELECT COUNT(*) > 0 FROM tbl_user u WHERE u.state = :state AND u.phone = :phone", nativeQuery = true)
    long existsUserInactiveByPhone(@Param("phone") String phone, @Param("state") String state);

    @Modifying
    @Query(value = "SELECT * FROM tbl_user u WHERE u.state = :state AND u.deactivated_at < :date", nativeQuery = true)
    List<User> findInactiveUsersBeforeDate(@Param("state") String state, @Param("date") LocalDate date);
}
