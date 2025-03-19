package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.TokenType;
import spring_devjob.dto.request.EmailRequest;
import spring_devjob.dto.request.ResetPasswordRequest;
import spring_devjob.entity.ForgotPasswordToken;
import spring_devjob.entity.User;
import spring_devjob.entity.VerificationCodeEntity;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.ForgotPasswordTokenRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.VerificationCodeRepository;
import spring_devjob.repository.history.UserHistoryRepository;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountRecoveryService {
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final TokenService tokenService;
    private final UserHistoryRepository userHistoryRepository;

    @Value("${jwt.reset.expiry-in-minutes}")
    protected long resetTokenExpiration;

    public VerificationCodeEntity forgotPassword(EmailRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = generateVerificationCode();
        try {
            emailService.sendPasswordResetCode(user, verificationCode);

            long expirationTimeInMinutes = System.currentTimeMillis() / 60000 + (5);

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.builder()
                    .email(user.getEmail())
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTimeInMinutes)
                    .build();

            return saveVerificationCode(verificationCodeEntity);
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private VerificationCodeEntity saveVerificationCode(VerificationCodeEntity code){
        Optional<VerificationCodeEntity> entityOptional = verificationCodeRepository.findByEmail(code.getEmail());
        if(entityOptional.isEmpty()){
            return verificationCodeRepository.save(code);
        }
        VerificationCodeEntity entity = entityOptional.get();
        entity.setVerificationCode(code.getVerificationCode());
        entity.setExpirationTime(code.getExpirationTime());

        return entity;
    }

    public ForgotPasswordToken verifyCode(String email, String verificationCode) throws JOSEException {
            VerificationCodeEntity verificationCodeEntity = verificationCodeRepository.findByEmailAndVerificationCode(email, verificationCode)
                    .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

            if (verificationCodeEntity.getExpirationTime() < System.currentTimeMillis() / 60000) {
                throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
            }

            User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

            String forgotPasswordToken = tokenService.generateToken(user, TokenType.RESET_PASSWORD_TOKEN);
            ForgotPasswordToken token = ForgotPasswordToken.builder()
                    .email(email)
                    .forgotPasswordToken(forgotPasswordToken)
                    .expiryTime(LocalDateTime.now().plusMinutes(resetTokenExpiration))
                    .build();

            verificationCodeRepository.delete(verificationCodeEntity);

            return forgotPasswordTokenRepository.save(token);
    }

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

        User user = userRepository.findByEmail(forgotPasswordToken.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        forgotPasswordTokenRepository.delete(forgotPasswordToken);
    }

    public VerificationCodeEntity recoverAccount(String email) {
        if(userHistoryRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.USER_ARCHIVED_IN_HISTORY);
        }
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getState() == EntityStatus.ACTIVE) {
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        String verificationCode = generateVerificationCode();
        try {
            emailService.sendAccountRecoveryCode(user, verificationCode);

            long expirationTimeInMinutes = System.currentTimeMillis() / 60000 + (10);

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.builder()
                    .email(user.getEmail())
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTimeInMinutes)
                    .build();

            return saveVerificationCode(verificationCodeEntity);
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional
    public void verifyRecoverAccountCode(String email, String verificationCode) {
        VerificationCodeEntity verificationCodeEntity = verificationCodeRepository.findByEmailAndVerificationCode(email, verificationCode)
                .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (verificationCodeEntity.getExpirationTime() < System.currentTimeMillis() / 60000) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setState(EntityStatus.ACTIVE);
        user.setDeactivatedAt(null);
        userRepository.save(user);

        verificationCodeRepository.delete(verificationCodeEntity);
    }


    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
