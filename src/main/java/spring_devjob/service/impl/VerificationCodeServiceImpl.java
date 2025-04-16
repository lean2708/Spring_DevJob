package spring_devjob.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spring_devjob.constants.VerificationType;
import spring_devjob.entity.RedisVerificationCode;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.RedisVerificationCodeRepository;
import spring_devjob.service.RedisVerificationCodeService;

import java.time.LocalDateTime;

@Service
@Slf4j(topic = "VERIFICATION-CODE-SERVICE")
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements RedisVerificationCodeService {

    private final RedisVerificationCodeRepository redisVerificationCodeRepository;

    @Value("${app.forgot-password.verification-code.expiration-minutes}")
    private long forgotPasswordExpiration;
    @Value("${app.recover-account.verification-code.expiration-minutes}")
    private long recoverAccountExpiration;

    // redis ghi de theo key (email)
    @Transactional
    @Override
    public RedisVerificationCode saveVerificationCode(String email, VerificationType type) {
        String verificationCode = generateVerificationCode();

        LocalDateTime expirationTime = getExpirationTimeByType(type);

        long ttl = java.time.Duration.between(LocalDateTime.now(), expirationTime).getSeconds();

        String redisKey = email + ":" + type;

        RedisVerificationCode code = RedisVerificationCode.builder()
                .redisKey(redisKey)
                .email(email)
                .verificationCode(verificationCode)
                .verificationType(type)
                .expirationTime(expirationTime)
                .ttl(ttl)
                .build();

        return redisVerificationCodeRepository.save(code);
    }

    @Override
    public RedisVerificationCode getVerificationCode(String email, VerificationType type, String verificationCode){
        String redisKey = email + ":" + type;

       RedisVerificationCode redisVerificationCode = redisVerificationCodeRepository
                .findById(redisKey)
                .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (!redisVerificationCode.getVerificationCode().equals(verificationCode)) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_INVALID);
        }

        if (redisVerificationCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        return redisVerificationCode;
    }

    private LocalDateTime getExpirationTimeByType(VerificationType type){
        switch (type){
            case FORGOT_PASSWORD -> {
                return LocalDateTime.now().plusMinutes(forgotPasswordExpiration);
            }
            case RECOVER_ACCOUNT -> {
                return LocalDateTime.now().plusMinutes(recoverAccountExpiration);
            }
            default -> throw new AppException(ErrorCode.CODE_TYPE_INVALID);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

}
