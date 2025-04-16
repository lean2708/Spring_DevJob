package spring_devjob.service;


import spring_devjob.constants.VerificationType;
import spring_devjob.entity.RedisVerificationCode;

public interface RedisVerificationCodeService {

    RedisVerificationCode saveVerificationCode(String email, VerificationType type);

    RedisVerificationCode getVerificationCode(String email, VerificationType type, String verificationCode);
}
