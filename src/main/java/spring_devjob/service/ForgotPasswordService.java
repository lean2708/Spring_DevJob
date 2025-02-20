package spring_devjob.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring_devjob.dto.request.ChangePasswordRequest;
import spring_devjob.dto.request.ForgotPasswordRequest;
import spring_devjob.entity.User;
import spring_devjob.entity.VerificationCodeEntity;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.UserRepository;
import spring_devjob.repository.VerificationCodeRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;


    public VerificationCodeEntity forgotPassword(ForgotPasswordRequest request){
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String verificationCode = generateVerificationCode();
        try {
            emailService.sendVerificationCode(user, verificationCode);

            long expirationTimeInMinutes = System.currentTimeMillis() / 60000 + (10);

            VerificationCodeEntity verificationCodeEntity = VerificationCodeEntity.builder()
                    .email(user.getEmail())
                    .verificationCode(verificationCode)
                    .expirationTime(expirationTimeInMinutes)
                    .build();

            verificationCodeRepository.save(verificationCodeEntity);

            return verificationCodeEntity;
        } catch (Exception e) {
            log.error("Lỗi gửi email: ", e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public boolean verifyCode(String email, String verificationCode) {
            VerificationCodeEntity verificationCodeEntity = verificationCodeRepository.findByEmailAndVerificationCode(email, verificationCode)
                    .orElseThrow(() -> new AppException(ErrorCode.VERIFICATION_CODE_NOT_FOUND));

            if (verificationCodeEntity.getExpirationTime() < System.currentTimeMillis() / 60000) {
                throw new AppException(ErrorCode.VERIFICATION_CODE_EXPIRED);
            }

            return true;
    }


    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredVerificationCodes() {
        long currentTimeInMinutes = System.currentTimeMillis() / 60000; // thoi diem hien tai (phut)

        verificationCodeRepository.deleteByExpirationTimeBefore(currentTimeInMinutes);
    }

}
