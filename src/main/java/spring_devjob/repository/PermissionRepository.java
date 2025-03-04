package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Permission;
import spring_devjob.entity.Role;

import java.util.Collection;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {

    boolean existsByName(String name);

    List<Permission> findAllByNameIn(List<String> names);

    List<Permission> findAllByIdIn(List<Long> ids);

    List<Permission> findAllByRolesIn(List<Role> roleList);
}
