package spring_devjob.config;


import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private final CustomJwtDecoder customJwtDecoder;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    private final String[] PUBLIC_URLS  = {
            "/v1/auth/login", "/v1/auth/register", "/v1/auth/logout",
            "/v1/auth/forgot-password", "/v1/auth/forgot-password/verify-code",
            "/v1/auth/forgot-password/reset-password"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(request -> request.requestMatchers(PUBLIC_URLS).permitAll()
                        .anyRequest().authenticated())

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(
                                        jwtConfigurer -> jwtConfigurer
                                                .decoder(customJwtDecoder)
                                                .jwtAuthenticationConverter(customJwtAuthenticationConverter)
                                )
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                );

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // cho phép yêu cầu từ cac cong
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173",
                "http://localhost:5174"
        ));

        corsConfiguration.addAllowedMethod("*"); // cho phép tất cả method
        corsConfiguration.addAllowedHeader("*"); // cho phép tất cả header
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration); // dang ki cau hinh

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

    // thiet lap url tren giao dien browser
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web ->
                web.ignoring().requestMatchers("/actuator/**","/v3/**", "webjar/**",
                        "/swagger-ui*/*swagger-initializer.js","/swagger-ui*/**");
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
