package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Role;
import spring_devjob.entity.relationship.RoleHasPermission;

@Repository
public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission,Long> {
    void deleteByRole(Role role);
}
