package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.Subscriber;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber,Long>  {

    Optional<Subscriber> findByEmail(String email);

    Set<Subscriber> findAllByIdIn(Set<Long> ids);
}
