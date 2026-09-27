package com.example.fitmanager.dto;

import java.util.List;


public class ExerciseRecommendationRequest {

    // Fields

    private String query;

    private String bodyPart;
    private String difficulty;
    private List<String> equipment;
    private String goal;

    private Integer limit;


    // Constructors
    // ------------------------------------------------------------

    public ExerciseRecommendationRequest() {
        //
    }


    // Getters and Setters
    // ------------------------------------------------------------

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(final String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(final List<String> equipment) {
        this.equipment = equipment;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(final String goal) {
        this.goal = goal;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }
}
