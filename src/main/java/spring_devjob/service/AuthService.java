package spring_devjob.service;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring_devjob.constants.RoleEnum;
import spring_devjob.constants.TokenType;
import spring_devjob.dto.basic.EntityBasic;
import spring_devjob.dto.request.*;
import spring_devjob.dto.response.GoogleTokenResponse;
import spring_devjob.dto.response.GoogleUserResponse;
import spring_devjob.dto.response.TokenResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.Token;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.TokenRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.client.GoogleAuthClient;
import spring_devjob.client.GoogleUserInfoClient;
import spring_devjob.service.relationship.UserHasRoleService;

import java.text.ParseException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final UserHasRoleService userHasRoleService;
    private final GoogleAuthClient googleAuthClient;
    private final GoogleUserInfoClient googleUserInfoClient;

    @Value("${oauth2.google.client-id}")
    private String CLIENT_ID;
    @Value("${oauth2.google.client-secret}")
    private String CLIENT_SECRET;
    @Value("${oauth2.google.redirect-uri}")
    private String REDIRECT_URI;
    @Value("${oauth2.google.grant-type}")
    private String GRANT_TYPE;

    public TokenResponse authenticateWithGoogle(String code) throws JOSEException {
        GoogleTokenResponse response = googleAuthClient.exchangeToken(GoogleTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        // get user info with google
        GoogleUserResponse userInfo = googleUserInfoClient.getUserInfo("json", response.getAccessToken());

        // Onboard user
        User user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> {
                    User newUser = userRepository.save(User.builder()
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .avatarUrl(userInfo.getPicture())
                            .build());

            newUser.setRoles(new HashSet<>(Set.of(userHasRoleService.saveUserHasRole(newUser, RoleEnum.USER))));

                    return newUser;
        });

        return generateAndSaveTokenResponse(user);
    }

    public TokenResponse login(LoginRequest request) throws JOSEException {
        User userDB = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), userDB.getPassword());

        if(!isAuthenticated){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return generateAndSaveTokenResponse(userDB);
    }

    public TokenResponse register(RegisterRequest request) throws JOSEException {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.existsByPhone(request.getPhone())){
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        User user = userMapper.registerToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        user.setRoles(new HashSet<>(Set.of(userHasRoleService.saveUserHasRole(user, RoleEnum.USER))));

        emailService.sendUserEmailWithRegister(userRepository.save(user));

        return generateAndSaveTokenResponse(user);
    }

    public UserResponse getMyInfo() {
        User user = userRepository.findByEmail(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public TokenResponse refreshToken(TokenRequest request) throws ParseException, JOSEException {
        // check token
        Token token = tokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        // verify refresh token
        SignedJWT  signedJWT = tokenService.verifyToken(request.getRefreshToken(), TokenType.REFRESH_TOKEN);

        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // new access token
        String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);
        token.setAccessToken(accessToken);

        tokenService.saveToken(token);

        List<EntityBasic> entityBasics = user.getRoles().stream()
                .map(roleMapper::userHasRoleToEntityBasic).toList();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .email(email)
                .roles(entityBasics)
                .build();
    }

    public void changePassword(String oldPassword, String newPassword){
        User user = userRepository.findByEmail(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void logout(TokenRequest request) {
        // check token
        Token token = tokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REFRESH_TOKEN));

        tokenRepository.delete(token);
    }

    private TokenResponse generateAndSaveTokenResponse(User user) throws JOSEException {
        String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);

        String refreshToken = tokenService.generateToken(user, TokenType.REFRESH_TOKEN);

        List<EntityBasic> entityBasics = user.getRoles().stream()
                .map(roleMapper::userHasRoleToEntityBasic).toList();

        tokenService.saveToken(Token.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .email(user.getEmail())
                .roles(entityBasics)
                .build();
    }

    // info tu access token
    public String getCurrentUsername(){
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName(); // email
    }
}
