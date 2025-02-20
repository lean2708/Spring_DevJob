package spring_devjob.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.dto.basic.CompanyBasic;
import spring_devjob.dto.basic.RoleBasic;
import spring_devjob.dto.request.LoginRequest;
import spring_devjob.dto.request.RegisterRequest;
import spring_devjob.dto.request.TokenRequest;
import spring_devjob.dto.request.UserRequest;
import spring_devjob.dto.response.IntrospectResponse;
import spring_devjob.dto.response.LoginResponse;
import spring_devjob.dto.response.UserResponse;
import spring_devjob.entity.RevokedToken;
import spring_devjob.entity.Role;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.CompanyMapper;
import spring_devjob.mapper.RoleMapper;
import spring_devjob.mapper.UserMapper;
import spring_devjob.repository.RevokedTokenRepository;
import spring_devjob.repository.RoleRepository;
import spring_devjob.repository.UserRepository;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;
    private final RoleMapper roleMapper;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Value("${jwt.signerKey}")
    protected String SINGER_KEY;
    @Value("${jwt.validity-in-days}")
    protected long tokenExpiration;

    public LoginResponse login(LoginRequest request) throws JOSEException {
        User userDB = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder  = new BCryptPasswordEncoder(10);
        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), userDB.getPassword());

        if(!isAuthenticated){
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = generateToken(userDB);

        List<String> roleNames = userDB.getRoles()
                .stream().map(Role::getName).collect(Collectors.toList());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .authenticated(true)
                .email(userDB.getEmail())
                .role(roleNames)
                .build();
    }

    public UserResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName("USER").orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        List<Role> roles = new ArrayList<>();
        if(user.getRoles() != null){
            roles.addAll(user.getRoles());
        }
        roles.add(userRole);
        user.setRoles(roles);

        emailService.sendUserEmailWithRegister(user);

        return convertUserResponse(userRepository.save(user));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return convertUserResponse(user);
    }

    public String generateToken(User user) throws JOSEException {
        // header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // payload
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer(user.getName())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(tokenExpiration, ChronoUnit.DAYS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        jwsObject.sign(new MACSigner(SINGER_KEY.getBytes()));

        return jwsObject.serialize();
    }

    public String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(
                    role -> {stringJoiner.add("ROLE_" + role.getName());
            });
        }
        return stringJoiner.toString();
    }

    public IntrospectResponse introspect(TokenRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean inValid = true;
        try {
            verifyToken(token);
        } catch (AppException e){
            inValid = false;
        }
        return IntrospectResponse.builder()
                .valid(inValid)
                .build();
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SINGER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isVerifed = signedJWT.verify(verifier);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

        if(!isVerifed && !expityTime.after(new Date()) || jwtId == null){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(revokedTokenRepository.existsById(jwtId)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return signedJWT;
    }

    public static String getCurrentUserLogin(){
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        return authentication.getName();
    }

    public void logout(TokenRequest request) throws ParseException, JOSEException {
        SignedJWT signToken = verifyToken(request.getToken());
        String wti = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        RevokedToken revokedToken = RevokedToken.builder()
                .id(wti)
                .expiryTime(expiryTime)
                .build();

        revokedTokenRepository.save(revokedToken);
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
