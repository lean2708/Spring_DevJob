package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Role;
import spring_devjob.entity.relationship.RoleHasPermission;
import spring_devjob.entity.relationship.SubHasSkill;

import java.util.List;

@Repository
public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission,Long> {
    void deleteByRole(Role role);

    @Query("SELECT rhp FROM RoleHasPermission rhp WHERE rhp.role.id = :roleId")
    List<RoleHasPermission> findByRoleId(@Param("roleId") Long roleId);
}
