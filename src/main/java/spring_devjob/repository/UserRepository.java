package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    Set<User> findAllByIdIn(Set<Long> ids);

    @Query(value = "SELECT COUNT(*) FROM tbl_user WHERE email = :email", nativeQuery = true)
    Integer countByEmail(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) FROM tbl_user WHERE id = :id AND state = :state", nativeQuery = true)
    Integer countById(@Param("id") Long id, @Param("state") String state);

    @Query(value = "SELECT COUNT(*) FROM tbl_user WHERE email = :email AND state = :state", nativeQuery = true)
    Integer countByEmailAndState(@Param("email") String email, @Param("state") String state);

    @Query(value = "SELECT COUNT(*) FROM tbl_user WHERE phone = :phone AND state = :state", nativeQuery = true)
    Integer countByPhoneAndState(@Param("phone") String phone, @Param("state") String state);

    @Query(value = "SELECT * FROM tbl_user WHERE id = :id AND state = :state", nativeQuery = true)
    Optional<User> findUserById(@Param("id") Long id, @Param("state") String state);

    @Query(value = "SELECT * FROM tbl_user WHERE email = :email AND state = :state", nativeQuery = true)
    Optional<User> findUserByEmail(@Param("email") String email, @Param("state") String state);

    @Modifying
    @Query(value = "UPDATE tbl_user SET state = :state WHERE company_id = :companyId", nativeQuery = true)
    void updateUserStateByCompany(@Param("companyId") Long companyId, @Param("state") String state);

}
