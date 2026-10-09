package com.example.fitmanager.dto;

import java.time.LocalDate;
import java.util.Set;

import com.example.fitmanager.entity.TrainerSpecialization;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class TrainerUpdateRequest {

    // Fields

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "[0-9]{10}", message = "Phone number must contain exactly 10 digits")
    private String phoneNumber;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 60, message = "Experience years cannot exceed 60")
    private Integer experienceYears;

    private Set<TrainerSpecialization> specializations;


    // Getters and Setters
    // -------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(final LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(final Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Set<TrainerSpecialization> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(final Set<TrainerSpecialization> specializations) {
        this.specializations = specializations;
    }
}
