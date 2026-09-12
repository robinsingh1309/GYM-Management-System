package com.example.fitmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class AuthenticationRequest {

    // Fields

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;


    // Constructors
    // -------------------------------------------------------

    public AuthenticationRequest() {
        //
    }


    // Getters and Setters
    // -------------------------------------------------------

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
