package com.example.fitmanager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.ExerciseRecommendationRequest;
import com.example.fitmanager.dto.ExerciseRecommendationResponse;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.ExerciseRecommendationService;
import com.example.fitmanager.service.ExerciseService;
import com.example.fitmanager.service.QdrantExerciseService;


@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {

    // Fields

    private final ExerciseService exerciseService;
    private final ExerciseRecommendationService exerciseRecommendationService;
    private final QdrantExerciseService qdrantExerciseService;


    // Constructors
    // ---------------------------------------------------------

    @Autowired
    public ExerciseController( //
            final ExerciseService exerciseService, //
            final ExerciseRecommendationService exerciseRecommendationService, //
            final QdrantExerciseService qdrantExerciseService) {

        this.exerciseService = exerciseService;
        this.exerciseRecommendationService = exerciseRecommendationService;
        this.qdrantExerciseService = qdrantExerciseService;
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
            final @RequestParam("limit") int limit) {

        return ResponseEntity.ok( //
                exerciseRecommendationService.recommend(q, limit) //
        );
    }

    @GetMapping("/{id}/qdrant-point")
    public ResponseEntity<Map<String, Object>> getQdrantPoint( //
            final @PathVariable("id") Long id) {

        return ResponseEntity.ok(qdrantExerciseService.getExercisePoint(id));
    }

    @GetMapping("/qdrant/status")
    public ResponseEntity<Map<String, Object>> getQdrantStatus() {

        return ResponseEntity.ok( //
                Map.of( //
                        "collectionExists", qdrantExerciseService.collectionExists(), //
                        "pointCount", qdrantExerciseService.getPointCount() //
                ) //
        );
    }

    @PostMapping("/{id}/qdrant")
    public ResponseEntity<Map<String, Object>> upsertExerciseToQdrant( //
            final @PathVariable("id") Long id) {

        qdrantExerciseService.upsertExercise(id);

        return ResponseEntity.ok( //
                Map.of( //
                        "message", "Exercise upserted to Qdrant", //
                        "exerciseId", id) //
        );
    }

    @PostMapping("/qdrant/status")
    public ResponseEntity<Map<String, Object>> syncExercisesWithQdrant() {

        final int syncedCount = qdrantExerciseService.syncAllExercises();
        return ResponseEntity.ok( //
                Map.of( //
                        "message", "Exercises synchronized with Qdrant", //
                        "syncedCount", syncedCount, //
                        "qdrantPointCount", qdrantExerciseService.getPointCount()) //
        );
    }

    @PostMapping("/qdrant/recommend")
    public ResponseEntity<List<ExerciseRecommendationResponse>> recommendUsingQdrant( //
            @RequestBody final ExerciseRecommendationRequest request) {

        return ResponseEntity.ok(qdrantExerciseService.recommend(request));
    }

}
