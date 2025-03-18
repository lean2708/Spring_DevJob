package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.relationship.SubHasSkill;

@Repository
public interface SubHasSkillRepository extends JpaRepository<SubHasSkill,Long> {
}
