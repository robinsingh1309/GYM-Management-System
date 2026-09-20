package com.example.fitmanager.dto;

import com.example.fitmanager.entity.Exercise;


public class ExerciseRecommendationResponse {

    // Fields

    private Exercise exercise;
    private double similarityScore;


    // Constructors
    // -------------------------------------------------------------

    public ExerciseRecommendationResponse( //
            final Exercise exercise, final double similarityScore) {
        this.exercise = exercise;
        this.similarityScore = similarityScore;
    }


    // Getters and Setters
    // -------------------------------------------------------------

    public Exercise getExercise() {
        return exercise;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }
}
