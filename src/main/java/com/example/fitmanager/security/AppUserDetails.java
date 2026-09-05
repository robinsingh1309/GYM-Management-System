package com.example.fitmanager.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.fitmanager.entity.User;


@SuppressWarnings("serial")
public class AppUserDetails implements UserDetails {

    // Fields

    private final User user;


    // Constructors
    // ----------------------------------------------

    public AppUserDetails(final User user) {
        this.user = user;
    }


    // Methods
    // ----------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRole() == null) {
            return List.of();
        }

        return List.of( //
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()) //
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.getActive());
    }

}
