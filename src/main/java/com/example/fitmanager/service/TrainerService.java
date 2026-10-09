package com.example.fitmanager.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.dto.TrainerCreateRequest;
import com.example.fitmanager.dto.TrainerResponse;
import com.example.fitmanager.dto.TrainerUpdateRequest;
import com.example.fitmanager.entity.TrainerSpecialization;


public interface TrainerService {

    TrainerResponse createTrainer(TrainerCreateRequest request, boolean includeLinkedUser);

    TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request, boolean includeLinkedUser);

    void activateTrainer(Long id);

    void deactivateTrainer(Long id);

    void deleteTrainer(Long id);

    void restoreTrainer(Long id);

    void linkUser(Long trainerId, Long userId);

    void unlinkUser(Long trainerId);

    TrainerResponse getTrainerById(Long id, boolean includeLinkedUser);

    Page<TrainerResponse> getTrainers(TrainerFilter filter, int page, int size, //
            String sort, boolean includeLinkedUser);

    List<TrainerSpecialization> getSpecializations();
}
