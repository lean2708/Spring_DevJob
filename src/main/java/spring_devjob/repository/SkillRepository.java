package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Skill;

import java.util.List;
import java.util.Set;

@Repository
public interface SkillRepository extends JpaRepository<Skill,Long> {

    boolean existsByName(String name);

    Set<Skill> findAllByIdIn(Set<Long> ids);
}
