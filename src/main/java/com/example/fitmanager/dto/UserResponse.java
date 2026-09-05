package com.example.fitmanager.dto;

import java.time.LocalDateTime;

import com.example.fitmanager.entity.Role;


public class UserResponse {

    // Fields

    private Long id;

    private String userEmail;
    private Role role;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    // -------------------------------------------------------

    public UserResponse() {
        //
    }

    public UserResponse(final Long id, final String userEmail, //
            final Role role, final Boolean active, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt) {

        this.id = id;

        this.userEmail = userEmail;
        this.role = role;

        this.active = active;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters
    // -------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Role getRole() {
        return role;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
