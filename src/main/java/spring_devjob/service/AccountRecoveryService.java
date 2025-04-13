package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import spring_devjob.dto.request.EmailRequest;
import spring_devjob.dto.request.ResetPasswordRequest;
import spring_devjob.entity.ForgotPasswordToken;
import spring_devjob.entity.VerificationCodeEntity;

public interface AccountRecoveryService {

    VerificationCodeEntity forgotPassword(EmailRequest request);

    ForgotPasswordToken verifyForgotPasswordCode(String email, String verificationCode) throws JOSEException;

    void resetPassword(ResetPasswordRequest request);

    VerificationCodeEntity recoverAccount(String email);

    void verifyRecoverAccountCode(String email, String verificationCode);
}
