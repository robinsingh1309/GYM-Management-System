package com.example.fitmanager.config;

import java.time.LocalDateTime;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.example.fitmanager.exception.ErrorResponse;
import com.example.fitmanager.security.AppUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;


@Configuration
public class SecurityConfig {

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {

            final ErrorResponse errorResponse = new ErrorResponse( //
                    LocalDateTime.now(), HttpStatus.FORBIDDEN.value(), //
                    HttpStatus.FORBIDDEN.getReasonPhrase(), "You do not have permission to access this resource", //
                    request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");

            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), errorResponse);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider( //
            final AppUserDetailsService userDetailsService, //
            final PasswordEncoder passwordEncoder) {

        final DaoAuthenticationProvider authenticationProvider = //
                new DaoAuthenticationProvider(userDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain( //
            final HttpSecurity httpSecurity, //
            final DaoAuthenticationProvider provider, //
            final JwtAuthenticationConverter authenticationConverter, //
            final AccessDeniedHandler accessDeniedHandler) {

        httpSecurity.csrf(csrf -> csrf.disable()) //
                .authenticationProvider(provider) //
                .authorizeHttpRequests( //
                        auth -> auth.requestMatchers( //
                                "/api/v1/auth/register", //
                                "/api/v1/auth/login", //
                                "/api/v1/health" //
                        ).permitAll() //
                                .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN") //
                                .anyRequest().authenticated() //
                ) //
                .exceptionHandling( //
                        ex -> ex.accessDeniedHandler(accessDeniedHandler) //
                ) //
                .sessionManagement( //
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //
                ).oauth2ResourceServer( //
                        oauth2 -> oauth2.jwt( //
                                jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)//
                        ) //
                );


        return httpSecurity.build();
    }

    @Bean
    public SecretKey jwtSecretKey(final @Value("${jwt.secret}") String secretKey) {

        final byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    @Bean
    public AuthenticationManager authenticationManager(final DaoAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }


    @Bean
    public JwtAuthenticationConverter authenticationConverter() {

        final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");

        final JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }


    @Bean
    public JwtEncoder jwtEncoder(final SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(final SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
