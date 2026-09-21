package com.example.fitmanager.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.fitmanager.service.OAuth2HandoffService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Fields

    private final OAuth2HandoffService handoffService;
    private final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final String frontendBaseUrl;


    // Constructors
    // -------------------------------------------------------

    public OAuth2AuthenticationSuccessHandler( //
            final OAuth2HandoffService handoffService, //
            final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository, //
            final @Value("${frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {

        this.handoffService = handoffService;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.frontendBaseUrl = frontendBaseUrl;
    }


    // AuthenticationSuccessHandler Methods
    // -------------------------------------------------------

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, //
            final HttpServletResponse response, final Authentication authentication) //
            throws IOException, ServletException {

        authorizationRequestRepository.removeAuthorizationRequest(request, response);

        if (!(authentication.getPrincipal() instanceof FitManagerOidcUser user)) {
            response.sendRedirect(errorUrl("GOOGLE_AUTH_FAILED"));
            return;
        }

        final String code = handoffService.createCode(user.getUserId());
        response.sendRedirect(frontendBaseUrl + "/oauth2/callback?code=" + encode(code));
    }


    // Helper Methods
    // -------------------------------------------------------

    private String errorUrl(final String code) {
        return frontendBaseUrl + "/login?error=" + encode(code);
    }

    private String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
