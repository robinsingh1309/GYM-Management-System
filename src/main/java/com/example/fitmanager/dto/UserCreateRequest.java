package com.example.fitmanager.dto;

import com.example.fitmanager.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class UserCreateRequest {

    // Fields

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 100, //
            message = "Email must not exceed 100 characters")
    private String email;

    private String password;

    private String provider;

    private String providerSubject;

    @NotNull(message = "Role is required")
    private Role role;


    // Constructors
    // -------------------------------------------------------

    public UserCreateRequest() {
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(final String provider) {
        this.provider = provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public void setProviderSubject(final String providerSubject) {
        this.providerSubject = providerSubject;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(final Role role) {
        this.role = role;
    }
}
