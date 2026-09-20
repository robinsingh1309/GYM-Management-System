package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.dto.ExerciseRecommendationResponse;


public interface ExerciseRecommendationService {

    List<ExerciseRecommendationResponse> recommend(String query, int limit);

}
