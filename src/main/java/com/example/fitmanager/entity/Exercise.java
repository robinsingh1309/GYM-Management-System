package com.example.fitmanager.entity;

import java.util.List;


public class Exercise {

    // Fields

    private Long id;

    private String name;

    private String bodyPart;
    private String primaryMuscle;
    private List<String> secondaryMuscles;

    private List<String> equipment;

    private String difficulty;

    private String movementPattern;

    private String exerciseType;

    private List<String> goalTags;

    private String description;

    private List<String> instructions;

    private String embeddingText;

    private boolean active;


    // Constructors
    // ----------------------------------------------------

    public Exercise() {
        //
    }


    // Getters and Setters
    // ----------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(final String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getPrimaryMuscle() {
        return primaryMuscle;
    }

    public void setPrimaryMuscle(final String primaryMuscle) {
        this.primaryMuscle = primaryMuscle;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(final List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getEquipment() {
        return equipment;
    }

    public void setEquipment(final List<String> equipment) {
        this.equipment = equipment;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMovementPattern() {
        return movementPattern;
    }

    public void setMovementPattern(final String movementPattern) {
        this.movementPattern = movementPattern;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(final String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public List<String> getGoalTags() {
        return goalTags;
    }

    public void setGoalTags(final List<String> goalTags) {
        this.goalTags = goalTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(final List<String> instructions) {
        this.instructions = instructions;
    }

    public String getEmbeddingText() {
        return embeddingText;
    }

    public void setEmbeddingText(final String embeddingText) {
        this.embeddingText = embeddingText;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
