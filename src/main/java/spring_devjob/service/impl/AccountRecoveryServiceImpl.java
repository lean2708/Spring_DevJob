package spring_devjob.service.impl;

import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.TokenType;
import spring_devjob.constants.VerificationType;
import spring_devjob.dto.request.EmailRequest;
import spring_devjob.dto.request.ResetPasswordRequest;
import spring_devjob.dto.response.VerificationCodeResponse;
import spring_devjob.entity.ForgotPasswordToken;
import spring_devjob.entity.User;
import spring_devjob.entity.RedisVerificationCode;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.ForgotPasswordTokenRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.RedisVerificationCodeRepository;
import spring_devjob.service.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Optional;

import static spring_devjob.constants.VerificationType.FORGOT_PASSWORD;
import static spring_devjob.constants.VerificationType.RECOVER_ACCOUNT;

@Service
@Slf4j(topic = "ACCOUNT-RECOVERY-SERVICE")
@RequiredArgsConstructor
public class AccountRecoveryServiceImpl implements AccountRecoveryService {

    private final UserRepository userRepository;
    private final RedisVerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final TokenService tokenService;
    private final RestoreService restoreService;
    private final RedisVerificationCodeService redisVerificationCodeService;

    @Value("${jwt.reset.expiry-in-minutes}")
    private long resetTokenExpiration;

    @Override
    public VerificationCodeResponse forgotPassword(EmailRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        try {
            RedisVerificationCode redisVerificationCode = redisVerificationCodeService
                    .saveVerificationCode(user.getEmail(), FORGOT_PASSWORD);

            emailService.sendPasswordResetCode(user, redisVerificationCode.getVerificationCode());

            return VerificationCodeResponse.builder()
                    .email(redisVerificationCode.getEmail())
                    .verificationCode(redisVerificationCode.getVerificationCode())
                    .expirationTime(redisVerificationCode.getExpirationTime())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public ForgotPasswordToken verifyForgotPasswordCode(String email, String verificationCode) throws JOSEException {
        RedisVerificationCode redisVerificationCode = redisVerificationCodeService
                .getVerificationCode(email, FORGOT_PASSWORD, verificationCode);

            User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

            String forgotPasswordToken = tokenService.generateToken(user, TokenType.RESET_PASSWORD_TOKEN);
            ForgotPasswordToken token = ForgotPasswordToken.builder()
                    .email(email)
                    .forgotPasswordToken(forgotPasswordToken)
                    .expiryTime(LocalDateTime.now().plusMinutes(resetTokenExpiration))
                    .build();

            verificationCodeRepository.delete(redisVerificationCode);

            return forgotPasswordTokenRepository.save(token);
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        try {
            tokenService.verifyToken(request.getForgotPasswordToken(), TokenType.RESET_PASSWORD_TOKEN);
        } catch (JOSEException | ParseException e) {
            throw new BadJwtException(e.getMessage());
        } catch (AppException ex){
            throw new BadJwtException("Token không hợp lệ");
        }
        ForgotPasswordToken forgotPasswordToken = forgotPasswordTokenRepository
                .findByForgotPasswordToken(request.getForgotPasswordToken())
                .orElseThrow( () -> new AppException(ErrorCode.FORGOT_PASSWORD_TOKEN_NOT_FOUND));

        User user = userRepository.findByEmail(forgotPasswordToken.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        forgotPasswordTokenRepository.delete(forgotPasswordToken);
    }

    @Override
    public VerificationCodeResponse recoverAccount(String email) {
        if(userRepository.countByEmailAndState(email, EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }
        if(userRepository.countByEmailAndState(email, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.USER_DISABLED);
        }

        User user = userRepository.findUserByEmail(email, EntityStatus.LOCKED.name())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        try {
            RedisVerificationCode redisVerificationCode = redisVerificationCodeService
                    .saveVerificationCode(user.getEmail(), RECOVER_ACCOUNT);

            emailService.sendAccountRecoveryCode(user, redisVerificationCode.getVerificationCode());

            return VerificationCodeResponse.builder()
                    .email(redisVerificationCode.getEmail())
                    .verificationCode(redisVerificationCode.getVerificationCode())
                    .expirationTime(redisVerificationCode.getExpirationTime())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional
    @Override
    public void verifyRecoverAccountCode(String email, String verificationCode) {
        RedisVerificationCode redisVerificationCode = redisVerificationCodeService
                .getVerificationCode(email, RECOVER_ACCOUNT, verificationCode);

        User user = userRepository.findUserByEmail(email, EntityStatus.LOCKED.name())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        restoreService.restoreUser(user.getId(), EntityStatus.LOCKED);

        verificationCodeRepository.delete(redisVerificationCode);
    }
}
