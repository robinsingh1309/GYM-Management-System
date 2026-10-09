package com.example.fitmanager.dto;

import java.time.LocalDate;
import java.util.Set;

import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.TrainerStatus;


public class TrainerFilter {

    // Fields

    private String name;

    private String email;
    private String phoneNumber;

    private Set<TrainerSpecialization> specializations;

    private LocalDate joiningDateFrom;
    private LocalDate joiningDateTo;

    private Integer minExperienceYears;
    private Integer maxExperienceYears;

    private Boolean hasLinkedUser;
    private Long userId;

    private TrainerStatus status;


    // Getters and Setters
    // -------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public TrainerStatus getStatus() {
        return status;
    }

    public void setStatus(final TrainerStatus status) {
        this.status = status;
    }

    public Set<TrainerSpecialization> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(final Set<TrainerSpecialization> specializations) {
        this.specializations = specializations;
    }

    public LocalDate getJoiningDateFrom() {
        return joiningDateFrom;
    }

    public void setJoiningDateFrom(final LocalDate joiningDateFrom) {
        this.joiningDateFrom = joiningDateFrom;
    }

    public LocalDate getJoiningDateTo() {
        return joiningDateTo;
    }

    public void setJoiningDateTo(final LocalDate joiningDateTo) {
        this.joiningDateTo = joiningDateTo;
    }

    public Integer getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(final Integer minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public Integer getMaxExperienceYears() {
        return maxExperienceYears;
    }

    public void setMaxExperienceYears(final Integer maxExperienceYears) {
        this.maxExperienceYears = maxExperienceYears;
    }

    public Boolean getHasLinkedUser() {
        return hasLinkedUser;
    }

    public void setHasLinkedUser(final Boolean hasLinkedUser) {
        this.hasLinkedUser = hasLinkedUser;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
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
}
