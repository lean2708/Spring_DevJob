package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {

    boolean existsByName(String name);

    Set<Permission> findAllByNameIn(Set<String> names);

    List<Permission> findAllByIdIn(List<Long> ids);

    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id IN :roleIds")
    Set<Permission> findAllByRoleIds(@Param("roleIds") Set<Long> roleIds);

}
