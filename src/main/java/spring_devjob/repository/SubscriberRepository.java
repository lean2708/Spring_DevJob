package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Subscriber;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber,Long>  {
    boolean existsByEmail(String email);
    Subscriber findByEmail(String email);
    List<Subscriber> findAllByIdIn(List<Long> ids);
}
