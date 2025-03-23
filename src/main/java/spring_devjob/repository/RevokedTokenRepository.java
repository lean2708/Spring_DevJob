package spring_devjob.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.RedisRevokedToken;

import java.util.Optional;

@Repository
public interface RevokedTokenRepository extends CrudRepository<RedisRevokedToken, String> {
    boolean existsById(String accessToken);

}
