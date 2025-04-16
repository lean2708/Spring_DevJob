package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import spring_devjob.dto.request.EmailRequest;
import spring_devjob.dto.request.ResetPasswordRequest;
import spring_devjob.dto.response.VerificationCodeResponse;
import spring_devjob.entity.ForgotPasswordToken;
import spring_devjob.entity.RedisVerificationCode;

public interface AccountRecoveryService {

    VerificationCodeResponse forgotPassword(EmailRequest request);

    ForgotPasswordToken verifyForgotPasswordCode(String email, String verificationCode) throws JOSEException;

    void resetPassword(ResetPasswordRequest request);

    VerificationCodeResponse recoverAccount(String email);

    void verifyRecoverAccountCode(String email, String verificationCode);
}
