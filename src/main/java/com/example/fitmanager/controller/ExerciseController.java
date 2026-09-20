package com.example.fitmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.ExerciseRecommendationResponse;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.ExerciseRecommendationService;
import com.example.fitmanager.service.ExerciseService;


@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {

    // Fields

    private final ExerciseService exerciseService;
    private final ExerciseRecommendationService exerciseRecommendationService;


    // Constructors
    // ---------------------------------------------------------

    @Autowired
    public ExerciseController( //
            final ExerciseService exerciseService, //
            final ExerciseRecommendationService exerciseRecommendationService) {

        this.exerciseService = exerciseService;
        this.exerciseRecommendationService = exerciseRecommendationService;
    }


    // API End Points
    // ---------------------------------------------------------

    // GET

    @GetMapping
    public ResponseEntity<List<Exercise>> getAllExercises() {
        return ResponseEntity //
                .ok(exerciseService.getAllExercises());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById( //
            final @PathVariable("id") Long id) {

        final Exercise exerciseResponse = exerciseService.getExerciseById(id);
        return ResponseEntity //
                .status(HttpStatus.FOUND) //
                .body(exerciseResponse);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getExerciseCount() {
        final int exerciseTotalCount = exerciseService.getExerciseCount();

        return ResponseEntity.ok(exerciseTotalCount);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<ExerciseRecommendationResponse>> recommendExercises( //
            final @RequestParam("q") String q, //
            final @RequestParam("limit")int limit) {

        return ResponseEntity.ok( //
                exerciseRecommendationService.recommend(q, limit) //
        );
    }

}
