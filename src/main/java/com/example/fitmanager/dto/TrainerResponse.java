package com.example.fitmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.TrainerStatus;


public class TrainerResponse {

    // Fields

    private final Long id;

    private final String name;

    private final String email;
    private final String phoneNumber;

    private final LocalDate joiningDate;

    private final Integer experienceYears;
    private final Set<TrainerSpecialization> specializations;

    private final Boolean active;
    private final TrainerStatus status;

    private final Boolean deleted;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    // Constructors
    // -------------------------------------------------------

    public TrainerResponse(final Long id, final String name, final String email, //
            final String phoneNumber, final LocalDate joiningDate, //
            final Integer experienceYears, final Set<TrainerSpecialization> specializations, //
            final Boolean active, final Boolean deleted, final TrainerStatus status, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt) {

        this.id = id;

        this.name = name;

        this.email = email;
        this.phoneNumber = phoneNumber;

        this.joiningDate = joiningDate;

        this.experienceYears = experienceYears;
        this.specializations = specializations;

        this.active = active;
        this.status = status;

        this.deleted = deleted;

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

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public Set<TrainerSpecialization> getSpecializations() {
        return Collections.unmodifiableSet(specializations);
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public TrainerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
