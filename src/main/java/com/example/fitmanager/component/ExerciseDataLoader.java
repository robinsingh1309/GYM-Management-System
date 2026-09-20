package com.example.fitmanager.component;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.example.fitmanager.entity.Exercise;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;


@Component
public class ExerciseDataLoader {

    // Fields
    private final static String EXERCISE_RESOURCE_PATH = "data/exercise.json";

    private final ObjectMapper objectMapper;
    private final ExerciseCatalog exerciseCatalog;


    // Constructors
    // -----------------------------------------------------------------------------

    public ExerciseDataLoader(final ObjectMapper objectMapper, //
            final ExerciseCatalog exerciseCatalog) {

        this.objectMapper = objectMapper;
        this.exerciseCatalog = exerciseCatalog;
    }


    // Pre-Load Method Operations
    // -----------------------------------------------------------------------------

    @PostConstruct
    public void loadExercises() {

        try {
            final ClassPathResource resource = new ClassPathResource(EXERCISE_RESOURCE_PATH);

            try (final InputStream inputStream = resource.getInputStream()) {

                final ExerciseDataFile exerciseDataFile = objectMapper.readValue( //
                        inputStream, ExerciseDataFile.class);

                exerciseCatalog.loadExercises(exerciseDataFile.getExercises());
            }

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load exercises from data/exercise.json", exception);
        }
    }


    // Helper Class
    // -----------------------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExerciseDataFile {

        // Fields

        private List<Exercise> exercises;


        // Getters and Setters
        // -------------------------------------------------

        public List<Exercise> getExercises() {
            return exercises;
        }

        public void setExercises(final List<Exercise> exercises) {
            this.exercises = exercises;
        }
    }
}
