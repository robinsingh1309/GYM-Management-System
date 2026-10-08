package com.example.fitmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.example.fitmanager.entity.Role;
import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.TrainerStatus;


public class AdminTrainerResponse extends TrainerResponse {

    // Fields

    private final LinkedUserResponse linkedUser;


    // Constructors
    // -------------------------------------------------------

    public AdminTrainerResponse(final Long id, final String name, final String email, //
            final String phoneNumber, final LocalDate joiningDate, //
            final Integer experienceYears, final Set<TrainerSpecialization> specializations, //
            final Boolean active, final Boolean deleted, final TrainerStatus status, //
            final LocalDateTime createdAt, final LocalDateTime updatedAt, //
            final LinkedUserResponse linkedUser) {

        super(id, name, email, phoneNumber, joiningDate, experienceYears, specializations, //
                active, deleted, status, createdAt, updatedAt);

        this.linkedUser = linkedUser;
    }


    // Getters
    // -------------------------------------------------------

    public LinkedUserResponse getLinkedUser() {
        return linkedUser;
    }


    // Helper Classes
    // -------------------------------------------------------

    public static class LinkedUserResponse {

        // Fields

        private final Long id;
        private final String email;
        private final Role role;
        private final Boolean active;


        // Constructors
        // -------------------------------------------------------

        public LinkedUserResponse(final Long id, final String email, //
                final Role role, final Boolean active) {

            this.id = id;
            this.email = email;
            this.role = role;
            this.active = active;
        }


        // Getters
        // -------------------------------------------------------

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public Role getRole() {
            return role;
        }

        public Boolean getActive() {
            return active;
        }
    }
}
