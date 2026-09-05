package com.example.fitmanager.dto;

import com.example.fitmanager.entity.Role;


public class AuthenticationResponse {

    // Fields

    private Long userId;

    private String userEmail;
    private Role role;


    // Constructors
    // -------------------------------------------------------

    public AuthenticationResponse() {
        //
    }

    public AuthenticationResponse(final Long userId, //
            final String userEmail, final Role role) {

        this.userId = userId;

        this.userEmail = userEmail;
        this.role = role;
    }


    // Getters and Setters
    // -------------------------------------------------------

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Role getRole() {
        return role;
    }
}
