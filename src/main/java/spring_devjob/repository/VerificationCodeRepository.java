package spring_devjob.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring_devjob.constants.VerificationType;
import spring_devjob.entity.VerificationCodeEntity;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    @Query("SELECT v FROM VerificationCodeEntity v WHERE v.email = :email AND v.type = :type")
    Optional<VerificationCodeEntity> findByEmailAndType(@Param("email") String email, @Param("type") VerificationType type);

    @Query("SELECT v FROM VerificationCodeEntity v " +
            "WHERE v.email = :email AND v.verificationCode = :verificationCode AND v.type = :type")
    Optional<VerificationCodeEntity> findByEmailAndVerificationCode(@Param("email") String email,
                                                                    @Param("verificationCode") String verificationCode,
                                                                    @Param("type") VerificationType type);




}
