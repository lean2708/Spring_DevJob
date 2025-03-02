package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring_devjob.entity.VerificationCodeEntity;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findByEmailAndVerificationCode(String email, String verificationCode);

    Optional<VerificationCodeEntity> findByEmail(String email);
}
