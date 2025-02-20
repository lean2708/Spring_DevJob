package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.RevokedToken;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken,String> {
}
