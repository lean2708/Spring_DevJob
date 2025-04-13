package spring_devjob.service.impl;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.RoleEnum;
import spring_devjob.constants.TokenType;
import spring_devjob.dto.request.*;
import spring_devjob.dto.response.GoogleTokenResponse;
import spring_devjob.dto.response.GoogleUserResponse;
import spring_devjob.dto.response.TokenResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.PermissionRepository;
import spring_devjob.repository.UserRepository;
import spring_devjob.client.GoogleAuthClient;
import spring_devjob.client.GoogleUserInfoClient;
import spring_devjob.service.*;
import spring_devjob.service.relationship.UserHasRoleService;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j(topic = "AUTH-SERVICE")
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserHasRoleService userHasRoleService;
    private final GoogleAuthClient googleAuthClient;
    private final GoogleUserInfoClient googleUserInfoClient;
    private final RedisTokenService redisTokenService;
    private final PermissionRepository permissionRepository;
    private final UserAuthCacheService userAuthCacheService;
    private final EntityDeactivationService entityDeactivationService;
    private final CurrentUserService currentUserService;

    @Value("${oauth2.google.client-id}")
    private String CLIENT_ID;
    @Value("${oauth2.google.client-secret}")
    private String CLIENT_SECRET;
    @Value("${oauth2.google.redirect-uri}")
    private String REDIRECT_URI;
    @Value("${oauth2.google.grant-type}")
    private String GRANT_TYPE;

    @Override
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

    @Override
    public TokenResponse login(LoginRequest request) throws JOSEException {
        User userDB = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), userDB.getPassword());

        if(!isAuthenticated){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        return generateAndSaveTokenResponse(userDB);
    }

    @Override
    public TokenResponse register(RegisterRequest request) throws JOSEException {
        checkUserExistenceAndStatus(request.getEmail(), request.getPhone());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        User user = userMapper.registerToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        user.setRoles(new HashSet<>(Set.of(userHasRoleService.saveUserHasRole(user, RoleEnum.USER))));

        emailService.sendUserEmailWithRegister(userRepository.save(user));

        return generateAndSaveTokenResponse(user);
    }

    public  void checkUserExistenceAndStatus(String email, String phone){
        if(userRepository.countByEmailAndState(email, EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.ACTIVE.name()) > 0){
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }
        if(userRepository.countByEmailAndState(email, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_DISABLED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.INACTIVE.name()) > 0){
            throw new AppException(ErrorCode.PHONE_DISABLED);
        }
        if(userRepository.countByEmailAndState(email, EntityStatus.LOCKED.name()) > 0){
            throw new AppException(ErrorCode.EMAIL_LOCKED);
        }
        if(userRepository.countByPhoneAndState(phone, EntityStatus.LOCKED.name()) > 0){
            throw new AppException(ErrorCode.PHONE_LOCKED);
        }
    }

    public UserResponse getMyInfo() {
        User user = userRepository.findByEmail(currentUserService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @Override
    public TokenResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        // verify refresh token (db, expirationTime ...)
        SignedJWT  signedJWT = tokenService.verifyToken(request.getRefreshToken(), TokenType.REFRESH_TOKEN);

        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // new access token
        String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);

        processUserRolesAndPermissions(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .email(email)
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest request){
        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout(TokenRequest request) throws ParseException, JOSEException {
        SignedJWT signToken = tokenService.verifyToken(request.getAccessToken(), TokenType.ACCESS_TOKEN);

        String email = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        RedisRevokedToken redisRevokedToken = RedisRevokedToken.builder()
                .accessToken(request.getAccessToken())
                .email(email)
                .expiryTime(expiryTime)
                .ttl((expiryTime.getTime() - System.currentTimeMillis()) / 1000)
                .build();

        redisTokenService.saveRevokedToken(redisRevokedToken);
    }

    @Transactional
    @Override
    public void lockAccount(LockAccountRequest request) {
        User user = userRepository.findByEmail(currentUserService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        entityDeactivationService.deactivateUser(user, EntityStatus.LOCKED);

        log.info("Locked user");
    }


    private TokenResponse generateAndSaveTokenResponse(User user) throws JOSEException {
        String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);

        String refreshToken = tokenService.generateToken(user, TokenType.REFRESH_TOKEN);

        tokenService.saveRefreshToken(refreshToken);

        processUserRolesAndPermissions(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .email(user.getEmail())
                .build();
    }


    private void processUserRolesAndPermissions(User user) {
        Set<Long> roleIds = user.getRoles().stream()
                .map(UserHasRole::getRole)
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<String> permissions = permissionRepository.findAllByRoleIds(roleIds)
                .stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());

        userAuthCacheService.saveUserWithPermission(UserAuthCache.builder()
                .email(user.getEmail())
                .roleIds(roleIds)
                .permissions(permissions)
                .build());
    }


}
