package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    boolean existsByName(String name);

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.name = :name AND r.state = 'INACTIVE'")
    boolean existsInactiveRoleByName(@Param("name") String name);

    Optional<Role> findByName(String name);

    Set<Role> findAllByIdIn(Set<Long> ids);
}
