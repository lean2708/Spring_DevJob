package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.VerificationType;
import spring_devjob.entity.RedisVerificationCode;

import java.util.Optional;

@Repository
public interface RedisVerificationCodeRepository extends CrudRepository<RedisVerificationCode, String> {



}
