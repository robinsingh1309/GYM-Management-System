package com.example.fitmanager.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.entity.Role;


public class FitManagerOidcUser implements OidcUser {

    // Fields

    private final OidcUser oidcUser;
    private final Long userId;
    private final String fitManagerEmail;
    private final Role role;
    private final Boolean active;


    // Constructors
    // -------------------------------------------------------

    public FitManagerOidcUser(final OidcUser oidcUser, final UserResponse user) {
        this.oidcUser = oidcUser;
        this.userId = user.getId();
        this.fitManagerEmail = user.getUserEmail();
        this.role = user.getRole();
        this.active = user.getActive();
    }


    // OidcUser Methods
    // -------------------------------------------------------

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oidcUser.getAuthorities();
    }

    @Override
    public String getName() {
        return oidcUser.getName();
    }


    // Getters
    // -------------------------------------------------------

    public Long getUserId() {
        return userId;
    }

    public String getFitManagerEmail() {
        return fitManagerEmail;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }
}
