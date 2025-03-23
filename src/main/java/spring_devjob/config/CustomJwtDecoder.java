package spring_devjob.config;


import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import spring_devjob.constants.TokenType;
import spring_devjob.dto.request.TokenRequest;
import spring_devjob.exception.AppException;
import spring_devjob.service.AuthService;
import spring_devjob.service.TokenService;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final TokenService tokenService;
    private final NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // check token (signer key, blacklist, ...)
            tokenService.verifyToken(token, TokenType.ACCESS_TOKEN);
        } catch (JOSEException | ParseException e) {
            throw new BadJwtException(e.getMessage());
        } catch (AppException ex){
            throw new BadJwtException("Token không hợp lệ");
        }
        return nimbusJwtDecoder.decode(token);
    }

}

