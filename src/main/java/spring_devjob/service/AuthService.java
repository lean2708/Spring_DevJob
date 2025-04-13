package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import spring_devjob.dto.request.*;
import spring_devjob.dto.response.TokenResponse;
import spring_devjob.dto.response.UserResponse;

import java.text.ParseException;

public interface AuthService {

    TokenResponse register(RegisterRequest request) throws JOSEException;

    TokenResponse login(LoginRequest request) throws JOSEException;

    TokenResponse authenticateWithGoogle(String code) throws JOSEException;

    UserResponse getMyInfo();

    TokenResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;

    void changePassword(ChangePasswordRequest request);

    void logout(TokenRequest request) throws ParseException, JOSEException;

    void lockAccount(LockAccountRequest request);

}
