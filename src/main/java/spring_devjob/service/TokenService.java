package spring_devjob.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import spring_devjob.constants.TokenType;
import spring_devjob.entity.RefreshToken;
import spring_devjob.entity.User;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.repository.RefreshTokenRepository;
import spring_devjob.repository.RevokedTokenRepository;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;
    @Value("${jwt.access-token.expiry-in-minutes}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-key}")
    private String REFRESH_KEY;

    @Value("${jwt.refresh-token.expiry-in-days}")
    private long refreshTokenExpiration;

    @Value("${jwt.reset-key}")
    private String RESET_PASSWORD_KEY;

    @Value("${jwt.reset.expiry-in-minutes}")
    private long resetTokenExpiration;

    public String generateToken(User user, TokenType type) throws JOSEException {
        // Header
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        // Payload
        long durationInSeconds = getDurationByToken(type);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer(user.getName())
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(durationInSeconds)))
                .jwtID(UUID.randomUUID().toString())
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        // Signature
        jwsObject.sign(new MACSigner(getKey(type).getBytes()));

        return jwsObject.serialize();
    }

    private String getKey(TokenType type){
        switch (type){
            case ACCESS_TOKEN -> {return SIGNER_KEY;}
            case REFRESH_TOKEN -> {return REFRESH_KEY;}
            case RESET_PASSWORD_TOKEN -> {return RESET_PASSWORD_KEY;}
            default -> throw new AppException(ErrorCode.TOKEN_TYPE_INVALID);
        }
    }
    private long getDurationByToken(TokenType type) {
        switch (type) {
            case ACCESS_TOKEN -> {return Duration.ofMinutes(accessTokenExpiration).getSeconds();}
            case REFRESH_TOKEN -> {return Duration.ofDays(refreshTokenExpiration).getSeconds();}
            case RESET_PASSWORD_TOKEN -> {return Duration.ofMinutes(resetTokenExpiration).getSeconds();}
            default -> throw new AppException(ErrorCode.TOKEN_TYPE_INVALID);
        }
    }

    public SignedJWT verifyToken(String token, TokenType type) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(getKey(type).getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean isVerified = signedJWT.verify(verifier);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();

        if(!isVerified || expirationTime.before(new Date()) || jwtId == null){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // check accessToken (blacklist)
        if (type == TokenType.ACCESS_TOKEN && revokedTokenRepository.existsById(token)){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if(type == TokenType.REFRESH_TOKEN && !refreshTokenRepository.existsByRefreshToken(token)){
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        return signedJWT;
    }

    public void saveRefreshToken(String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .refreshToken(token)
                .expiryDate(LocalDateTime.now().plusDays(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(refreshToken);
    }


    @Scheduled(cron = "0 0 0 * * ?")
    public void removeExpiredRefreshTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

}
