package com.example.fitmanager.service;

import java.util.List;
import java.util.Map;

import com.example.fitmanager.dto.ExerciseRecommendationRequest;
import com.example.fitmanager.dto.ExerciseRecommendationResponse;

public interface QdrantExerciseService {

    boolean collectionExists();

    long getPointCount();

    Map<String, Object> getExercisePoint(Long exerciseId);

    void upsertExercise(Long exerciseId);

    int syncAllExercises();

    List<ExerciseRecommendationResponse> recommend(ExerciseRecommendationRequest request);
}
