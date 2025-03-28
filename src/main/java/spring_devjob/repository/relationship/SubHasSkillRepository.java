package spring_devjob.repository.relationship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.relationship.SubHasSkill;

import java.util.List;

@Repository
public interface SubHasSkillRepository extends JpaRepository<SubHasSkill,Long> {

    @Query("SELECT s FROM SubHasSkill s WHERE s.subscriber.id = :subscriberId")
    List<SubHasSkill> findBySubscriberId(@Param("subscriberId") Long subscriberId);

    @Query("SELECT s FROM SubHasSkill s WHERE s.skill.id = :skillId")
    List<SubHasSkill> findBySkillId(@Param("skillId") Long skillId);

}
