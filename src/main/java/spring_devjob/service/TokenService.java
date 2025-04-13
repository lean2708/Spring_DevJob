package spring_devjob.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import spring_devjob.constants.TokenType;
import spring_devjob.entity.User;

import java.text.ParseException;

public interface TokenService {

    String generateToken(User user, TokenType type) throws JOSEException;

    SignedJWT verifyToken(String token, TokenType type) throws JOSEException, ParseException;

    void saveRefreshToken(String token);
}
