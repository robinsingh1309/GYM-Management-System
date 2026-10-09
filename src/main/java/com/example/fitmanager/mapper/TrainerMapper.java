package com.example.fitmanager.mapper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.fitmanager.dto.AdminTrainerResponse;
import com.example.fitmanager.dto.AdminTrainerResponse.LinkedUserResponse;
import com.example.fitmanager.dto.TrainerCreateRequest;
import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.dto.TrainerResponse;
import com.example.fitmanager.dto.TrainerUpdateRequest;
import com.example.fitmanager.entity.Trainer;
import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.entity.User;
import com.example.fitmanager.exception.BadRequestException;


@Component
public class TrainerMapper {

    // Methods
    // -------------------------------------------------------

    public Trainer toEntity(final TrainerCreateRequest request, final boolean active) {

        return new Trainer(normalizeName(request.getName()), normalizeEmail(request.getEmail()), //
                request.getPhoneNumber(), request.getJoiningDate(), request.getExperienceYears(), //
                normalizeSpecializations(request.getSpecializations()), active);
    }

    public void updateEntity(final Trainer trainer, final TrainerUpdateRequest request) {

        trainer.setName(normalizeName(request.getName()));
        trainer.setEmail(normalizeEmail(request.getEmail()));
        trainer.setPhoneNumber(request.getPhoneNumber());
        trainer.setJoiningDate(request.getJoiningDate());
        trainer.setExperienceYears(request.getExperienceYears());
        trainer.setSpecializations(normalizeSpecializations(request.getSpecializations()));
    }

    public void normalizeFilter(final TrainerFilter filter) {

        if (Objects.nonNull(filter.getName())) {
            filter.setName(normalizeName(filter.getName()));
        }
        if (Objects.nonNull(filter.getEmail())) {
            filter.setEmail(normalizeEmail(filter.getEmail()));
        }
    }

    public TrainerResponse toResponse(final Trainer trainer, final boolean includeLinkedUser) {

        if (!includeLinkedUser) {
            return new TrainerResponse(trainer.getId(), trainer.getName(), trainer.getEmail(), //
                    trainer.getPhoneNumber(), trainer.getJoiningDate(), trainer.getExperienceYears(), //
                    trainer.getSpecializations(), trainer.getActive(), trainer.getDeleted(), //
                    trainer.getStatus(), trainer.getCreatedAt(), trainer.getUpdatedAt());
        }

        final User user = trainer.getUser();
        final LinkedUserResponse linkedUser = Objects.isNull(user) //
                ? null //
                : new LinkedUserResponse(user.getId(), user.getEmail(), user.getRole(), user.getActive());

        return new AdminTrainerResponse(trainer.getId(), trainer.getName(), trainer.getEmail(), //
                trainer.getPhoneNumber(), trainer.getJoiningDate(), trainer.getExperienceYears(), //
                trainer.getSpecializations(), trainer.getActive(), trainer.getDeleted(), //
                trainer.getStatus(), trainer.getCreatedAt(), trainer.getUpdatedAt(), linkedUser);
    }


    // Helper Methods
    // -------------------------------------------------------

    private String normalizeName(final String name) {

        if (Objects.isNull(name)) {
            throw new BadRequestException("Trainer name is required");
        }
        return name.strip().replaceAll("(?U)\\s+", " ");
    }

    private String normalizeEmail(final String email) {

        if (Objects.isNull(email)) {
            throw new BadRequestException("Trainer email is required");
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private Set<TrainerSpecialization> normalizeSpecializations( //
            final Set<TrainerSpecialization> specializations) {

        return Objects.isNull(specializations) ? new HashSet<>() : new HashSet<>(specializations);
    }
}
