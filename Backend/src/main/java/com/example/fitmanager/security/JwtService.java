package com.example.fitmanager.security;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.TokenResponse;


@Service
public class JwtService {

    // Fields

    @Value("${jwt.expiry}")
    private Long expirationInSecs;

    @Value("${jwt.issuer}")
    private String issuer;

    private final JwtEncoder jwtEncoder;


    // Constructors
    // ---------------------------------------------------------

    @Autowired
    public JwtService(final JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }


    // Methods
    // ---------------------------------------------------------

    public TokenResponse generateToken(final Authentication authentication) {

        final Instant now = Instant.now();

        final List<String> authorities = authentication.getAuthorities() //
                .stream() //
                .map(GrantedAuthority::getAuthority) //
                .toList();

        final JwtClaimsSet claimsSet = JwtClaimsSet.builder() //
                .issuer(issuer) //
                .issuedAt(now) //
                .expiresAt(now.plusSeconds(expirationInSecs)) //
                .subject(authentication.getName()) //
                .claim("authorities", authorities) //
                .build();

        final Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet));

        final String loggedInMessage = "Logged in Successfully";
        final String jwtTokenValue = jwt.getTokenValue();

        final TokenResponse loginResponse = new TokenResponse(loggedInMessage, jwtTokenValue);
        return loginResponse;
    }

}
