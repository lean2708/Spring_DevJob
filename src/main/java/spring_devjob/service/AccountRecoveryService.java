package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.stereotype.Service;
import spring_devjob.constants.TokenType;
import spring_devjob.constants.VerificationType;
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
    private final RestoreService restoreService;

    @Value("${jwt.reset.expiry-in-minutes}")
    private long resetTokenExpiration;

    @Value("${app.forgot-password.verification-code.expiration-minutes}")
    private long forgotPasswordExpiration;

    @Value("${app.recover-account.verification-code.expiration-minutes}")
    private long recoverAccountExpiration;


    public VerificationCodeEntity forgotPassword(EmailRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = generateVerificationCode();
        try {
            emailService.sendPasswordResetCode(user, verificationCode);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(forgotPasswordExpiration);

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.builder()
                    .email(user.getEmail())
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTime)
                    .type(VerificationType.FORGOT_PASSWORD)
                    .build();

            return saveVerificationCode(verificationCodeEntity);
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private VerificationCodeEntity saveVerificationCode(VerificationCodeEntity code){
        Optional<VerificationCodeEntity> entityOptional = verificationCodeRepository
                .findByEmailAndType(code.getEmail(), code.getType());

        if (entityOptional.isPresent()) {
            VerificationCodeEntity entity = entityOptional.get();
            entity.setVerificationCode(code.getVerificationCode());
            entity.setExpirationTime(code.getExpirationTime());
            return verificationCodeRepository.save(entity);
        }

        return verificationCodeRepository.save(code);
    }

    public ForgotPasswordToken verifyForgotPasswordCode(String email, String verificationCode) throws JOSEException {
            VerificationCodeEntity verificationCodeEntity = verificationCodeRepository
                    .findByEmailAndVerificationCode(email, verificationCode, VerificationType.FORGOT_PASSWORD)
                    .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

            if (verificationCodeEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
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

    @Transactional
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
        if(userRepository.existsByEmail(email)){
            throw new AppException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = generateVerificationCode();
        try {
            emailService.sendAccountRecoveryCode(user, verificationCode);

            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(recoverAccountExpiration);

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.builder()
                    .email(user.getEmail())
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTime)
                    .type(VerificationType.RECOVER_ACCOUNT)
                    .build();

            return saveVerificationCode(verificationCodeEntity);
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional
    public void verifyRecoverAccountCode(String email, String verificationCode) {
        VerificationCodeEntity verificationCodeEntity = verificationCodeRepository
                .findByEmailAndVerificationCode(email, verificationCode, VerificationType.RECOVER_ACCOUNT)
                .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

        if (verificationCodeEntity.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        restoreService.restoreUser(user.getId());

        verificationCodeRepository.delete(verificationCodeEntity);
    }


    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
