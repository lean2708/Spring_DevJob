package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Subscriber;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber,Long>  {
    boolean existsByEmail(String email);
    Subscriber findByEmail(String email);
    @Query("SELECT DISTINCT s FROM Subscriber s JOIN s.skills skill WHERE skill.name IN :skillNames")
    List<Subscriber> findAllBySkillNames(@Param("skillNames") List<String> skillNames);
}
