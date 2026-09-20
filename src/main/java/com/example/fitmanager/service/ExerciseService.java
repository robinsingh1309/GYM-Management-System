package com.example.fitmanager.service;

import java.util.List;

import com.example.fitmanager.entity.Exercise;


public interface ExerciseService {

    List<Exercise> getAllExercises();

    Exercise getExerciseById(Long id);

    int getExerciseCount();

}
