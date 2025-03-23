package spring_devjob.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.UserAuthCache;

@Repository
public interface UserAuthCacheRepository extends CrudRepository<UserAuthCache, String> {
    boolean existsById(String email);
}
