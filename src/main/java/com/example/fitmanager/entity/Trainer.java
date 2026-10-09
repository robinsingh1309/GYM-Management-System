package com.example.fitmanager.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "trainers", uniqueConstraints = { //
        @UniqueConstraint(name = "uk_trainers_email", columnNames = "email"), //
        @UniqueConstraint(name = "uk_trainers_phone_number", columnNames = "phone_number"), //
        @UniqueConstraint(name = "uk_trainers_user_id", columnNames = "user_id") //
})
public class Trainer extends BaseEntity {

    // Fields

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "trainer_specializations", //
            joinColumns = @JoinColumn(name = "trainer_id", nullable = false), //
            uniqueConstraints = @UniqueConstraint( //
                    name = "uk_trainer_specializations_trainer_specialization", //
                    columnNames = {"trainer_id", "specialization"}))
    @Column(name = "specialization", nullable = false, length = 40)
    private Set<TrainerSpecialization> specializations = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, //
            foreignKey = @ForeignKey(name = "fk_trainers_user"))
    private User user;

    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;


    // Constructors
    // -------------------------------------------------------

    public Trainer() {
        //
    }

    public Trainer(final String name, final String email, final String phoneNumber, //
            final LocalDate joiningDate, final Integer experienceYears, //
            final Set<TrainerSpecialization> specializations, final Boolean active) {

        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;

        this.joiningDate = joiningDate;

        this.experienceYears = experienceYears;
        this.specializations = new HashSet<>(specializations);

        this.active = active;
        this.deleted = false;
    }


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
        this.specializations = new HashSet<>(specializations);
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public TrainerStatus getStatus() {

        if (Boolean.TRUE.equals(deleted)) {
            return TrainerStatus.DELETED;
        }

        return Boolean.TRUE.equals(active) ? TrainerStatus.ACTIVE : TrainerStatus.INACTIVE;
    }
}
