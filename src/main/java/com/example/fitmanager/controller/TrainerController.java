package com.example.fitmanager.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.fitmanager.dto.TrainerCreateRequest;
import com.example.fitmanager.dto.TrainerFilter;
import com.example.fitmanager.dto.TrainerResponse;
import com.example.fitmanager.dto.TrainerUpdateRequest;
import com.example.fitmanager.entity.TrainerSpecialization;
import com.example.fitmanager.service.TrainerService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/trainers")
public class TrainerController {

    // Fields

    private final TrainerService trainerService;


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public TrainerController(final TrainerService trainerService) {
        this.trainerService = trainerService;
    }


    // End Points
    // -------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<TrainerResponse> createTrainer( //
            @Valid @RequestBody final TrainerCreateRequest request) {

        final TrainerResponse response = trainerService.createTrainer(request, true);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequest() //
                .path("/{id}") //
                .buildAndExpand(response.getId()) //
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    // GET

    @GetMapping
    public ResponseEntity<Page<TrainerResponse>> getTrainers( //
            @ModelAttribute final TrainerFilter filter, //
            @RequestParam(name = "page", defaultValue = "0") final int page, //
            @RequestParam(name = "size", defaultValue = "20") final int size, //
            @RequestParam(name = "sort", defaultValue = "id,DESC") final String sort, //
            final Authentication authentication) {

        final Page<TrainerResponse> response = trainerService.getTrainers( //
                filter, page, size, sort, isAdmin(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/specializations")
    public ResponseEntity<List<TrainerSpecialization>> getSpecializations() {
        return ResponseEntity.ok(trainerService.getSpecializations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerResponse> getTrainer( //
            @PathVariable("id") final Long id, final Authentication authentication) {

        final TrainerResponse response = trainerService.getTrainerById(id, isAdmin(authentication));
        return ResponseEntity.ok(response);
    }

    // PUT

    @PutMapping("/{id}")
    public ResponseEntity<TrainerResponse> updateTrainer( //
            @PathVariable("id") final Long id, //
            @Valid @RequestBody final TrainerUpdateRequest request) {

        return ResponseEntity.ok(trainerService.updateTrainer(id, request, true));
    }

    @PutMapping("/{trainerId}/user/{userId}")
    public ResponseEntity<Void> linkUser( //
            @PathVariable("trainerId") final Long trainerId, //
            @PathVariable("userId") final Long userId) {

        trainerService.linkUser(trainerId, userId);
        return ResponseEntity.noContent().build();
    }

    // PATCH

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateTrainer(@PathVariable("id") final Long id) {
        trainerService.activateTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateTrainer(@PathVariable("id") final Long id) {
        trainerService.deactivateTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreTrainer(@PathVariable("id") final Long id) {
        trainerService.restoreTrainer(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(@PathVariable("id") final Long id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{trainerId}/user")
    public ResponseEntity<Void> unlinkUser(@PathVariable("trainerId") final Long trainerId) {
        trainerService.unlinkUser(trainerId);
        return ResponseEntity.noContent().build();
    }


    // Helper Methods
    // -------------------------------------------------------

    private boolean isAdmin(final Authentication authentication) {

        return authentication.getAuthorities().stream() //
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
