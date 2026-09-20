package com.example.fitmanager.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.fitmanager.entity.Exercise;


@Component
public class ExerciseCatalog {

    // Fields

    private final List<Exercise> exercises = new ArrayList<>();


    // Methods
    // --------------------------------------------------------

    public void loadExercises(final List<Exercise> exercises) {

        this.exercises.clear();

        this.exercises.addAll(exercises);
    }

    public int size() {
        return exercises.size();
    }

    // Getters
    // --------------------------------------------------------

    public List<Exercise> getExercises() {
        return Collections.unmodifiableList(exercises);
    }

}
