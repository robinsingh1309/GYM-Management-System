package com.example.fitmanager.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    // Fields

    private static final Set<String> ALLOWED_ERROR_CODES = Set.of( //
            "EMAIL_ALREADY_REGISTERED", //
            "ACCOUNT_INACTIVE", //
            "INVALID_GOOGLE_IDENTITY" //
    );

    private final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final String frontendBaseUrl;


    // Constructors
    // -------------------------------------------------------

    public OAuth2AuthenticationFailureHandler( //
            final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository, //
            final @Value("${frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {

        this.authorizationRequestRepository = authorizationRequestRepository;
        this.frontendBaseUrl = frontendBaseUrl;
    }


    // AuthenticationFailureHandler Methods
    // -------------------------------------------------------

    @Override
    public void onAuthenticationFailure(final HttpServletRequest request, //
            final HttpServletResponse response, final AuthenticationException exception) //
            throws IOException, ServletException {

        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        final String code = errorCode(exception);
        response.sendRedirect(frontendBaseUrl + "/login?error=" + //
                URLEncoder.encode(code, StandardCharsets.UTF_8));
    }


    // Helper Methods
    // -------------------------------------------------------

    private String errorCode(final AuthenticationException exception) {

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            final String code = oauth2Exception.getError().getErrorCode();

            if (ALLOWED_ERROR_CODES.contains(code)) {
                return code;
            }
        }

        return "GOOGLE_AUTH_FAILED";
    }
}
