package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.entity.relationship.UserHasRole;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole,Long> {
    void deleteByUser(User user);

    void deleteByUserAndRole(User user, Role role);

    UserHasRole findByUserAndRole(User user, Role role);
}
