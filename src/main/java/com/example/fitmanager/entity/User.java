package com.example.fitmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "users", uniqueConstraints = { //
        @UniqueConstraint( //
                name = "uk_users_provider_subject", //
                columnNames = { "provider", "provider_subject" }) //
})
public class User extends BaseEntity {

    // Fields

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_subject")
    private String providerSubject;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "active", nullable = false)
    private Boolean active = true;


    // Constructors
    // -------------------------------------------------------

    public User() {
        //
    }

    public User(final String email, final String password, //
            final String provider, final String providerSubject, //
            final Role role) {

        this.email = email;
        this.password = password;

        this.provider = provider;
        this.providerSubject = providerSubject;

        this.role = role;

        this.active = true;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }
}
