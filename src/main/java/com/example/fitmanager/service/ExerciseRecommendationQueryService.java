package com.example.fitmanager.service;

import com.example.fitmanager.dto.ExerciseRecommendationRequest;


public interface ExerciseRecommendationQueryService {

    void populateMissingFilters(ExerciseRecommendationRequest request);
}
