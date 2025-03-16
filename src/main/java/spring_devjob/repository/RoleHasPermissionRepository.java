package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Job;
import spring_devjob.entity.JobHasSkill;
import spring_devjob.entity.Role;
import spring_devjob.entity.RoleHasPermission;

@Repository
public interface RoleHasPermissionRepository extends JpaRepository<RoleHasPermission,Long> {
    void deleteByRole(Role role);
}
