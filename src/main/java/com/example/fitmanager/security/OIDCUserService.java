package com.example.fitmanager.security;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.service.AuthenticationService;


@Service
public class OIDCUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    // Fields

    private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
    private final AuthenticationService authenticationService;


    // Constructors
    // -------------------------------------------------------------------

    @Autowired
    public OIDCUserService(final AuthenticationService authenticationService) {
        this(authenticationService, new OidcUserService());
    }

    OIDCUserService(final AuthenticationService authenticationService, //
            final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService) {

        this.authenticationService = authenticationService;
        this.oidcUserService = oidcUserService;
    }


    // OAuth2UserService Methods
    // -------------------------------------------------------------------

    @Override
    public OidcUser loadUser(final OidcUserRequest userRequest) //
            throws OAuth2AuthenticationException {

        final OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        final String providerSubject = oidcUser.getSubject();
        final String email = oidcUser.getEmail();

        if (Objects.isNull(providerSubject) || providerSubject.isBlank() //
                || Objects.isNull(email) || email.isBlank()) {

            throw authenticationException("INVALID_GOOGLE_IDENTITY");
        }

        try {
            final UserResponse user = //
                    authenticationService.authenticateGoogleUser(providerSubject, email);

            return new FitManagerOidcUser(oidcUser, user);
        } catch (final BadRequestException exception) {
            throw authenticationException(exception.getMessage());
        }
    }


    // Helper Methods
    // -------------------------------------------------------------------

    private OAuth2AuthenticationException authenticationException(final String code) {
        return new OAuth2AuthenticationException(new OAuth2Error(code), code);
    }

}
