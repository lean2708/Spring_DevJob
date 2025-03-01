package spring_devjob.service;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spring_devjob.constants.RoleEnum;
import spring_devjob.constants.TokenType;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.request.*;
import spring_devjob.dto.response.TokenResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.Role;
import spring_devjob.entity.Token;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.TokenRepository;
import spring_devjob.repository.UserRepository;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;


    public TokenResponse login(LoginRequest request) throws JOSEException {
        User userDB = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), userDB.getPassword());

        if(!isAuthenticated){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        List<String> roleNames = userDB.getRoles()
                .stream().map(Role::getName).collect(Collectors.toList());

        String accessToken = tokenService.generateToken(userDB, TokenType.ACCESS_TOKEN);

        String refreshToken = tokenService.generateToken(userDB, TokenType.REFRESH_TOKEN);

        tokenService.saveToken(Token.builder()
                        .email(userDB.getEmail())
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                .build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .email(userDB.getEmail())
                .role(roleNames)
                .build();
    }

    public TokenResponse register(RegisterRequest request) throws JOSEException {
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleEnum.USER.name()).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        List<Role> roles = new ArrayList<>();
        if(user.getRoles() != null){
            roles.addAll(user.getRoles());
        }
        roles.add(userRole);
        user.setRoles(roles);

        emailService.sendUserEmailWithRegister(user);
        userRepository.save(user);

        List<String> roleNames = user.getRoles()
                .stream().map(Role::getName).collect(Collectors.toList());

        String accessToken = tokenService.generateToken(user, TokenType.ACCESS_TOKEN);

        String refreshToken = tokenService.generateToken(user, TokenType.REFRESH_TOKEN);

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
                .role(roleNames)
                .build();
    }

    public UserResponse getMyInfo() {
        User user = userRepository.findByEmail(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return convertUserResponse(user);
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

        List<String> roleNames = user.getRoles()
                .stream().map(Role::getName).collect(Collectors.toList());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .email(email)
                .role(roleNames)
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

    // info tu access token
    public String getCurrentUsername(){
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName(); // email
    }

    public UserResponse convertUserResponse(User user){
        UserResponse response = userMapper.toUserResponse(user);

        CompanyBasic companyBasic = (user.getCompany() != null) ?
                companyMapper.toCompanyBasic(user.getCompany()) : null;
        response.setCompany(companyBasic);

        List<RoleBasic> roleBasics = (user.getRoles() != null) ?
                roleMapper.toRoleBasics(user.getRoles()) : null;
        response.setRoles(roleBasics);

        return response;
    }
}
