package com.example.fitmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class MemberResponse {

    // Fields

    private Long id;

    private String name;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private LocalDate joiningDate;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    // Constructors
    // -------------------------------------------------------

    public MemberResponse() {}

    public MemberResponse(final Long id, final String name, final String email, //
            final String phoneNumber, final LocalDate dateOfBirth, final String gender, //
            final String address, final LocalDate joiningDate, final Boolean active, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt) {

        this.id = id;

        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
        this.joiningDate = joiningDate;

        this.active = active;

        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters
    // -------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
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
