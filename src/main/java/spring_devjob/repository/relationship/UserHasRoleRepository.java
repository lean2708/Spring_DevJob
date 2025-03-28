package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.UserHasRole;

import java.util.List;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole,Long> {
    void deleteByUser(User user);

    void deleteByUserAndRole(User user, Role role);

    List<UserHasRole> findByRoleId(Long roleId);

    @Query(value = "SELECT * FROM tbl_user_has_role WHERE user_id = :userId AND state = :state",
            nativeQuery = true)
    List<UserHasRole> findByUserIdAndState(@Param("userId") Long userId,
                                           @Param("state") String state);

}
