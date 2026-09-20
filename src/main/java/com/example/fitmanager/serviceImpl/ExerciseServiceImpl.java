package com.example.fitmanager.serviceImpl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.fitmanager.component.ExerciseCatalog;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.service.ExerciseService;


@Service
public class ExerciseServiceImpl implements ExerciseService {

    // Fields

    private final ExerciseCatalog exerciseCatalog;


    // Constructors
    // ----------------------------------------------------------

    public ExerciseServiceImpl( //
            final ExerciseCatalog exerciseCatalog) {

        this.exerciseCatalog = exerciseCatalog;
    }


    // Methods
    // ----------------------------------------------------------

    @Override
    public List<Exercise> getAllExercises() {
        return exerciseCatalog.getExercises();
    }

    @Override
    public Exercise getExerciseById(final Long id) {

        final Optional<Exercise> getExercise = //
                exerciseCatalog.getExercises() //
                        .stream() //
                        .filter(exercise -> exercise.getId() == id) //
                        .findFirst();

        if (Objects.isNull(getExercise)) {
            throw new ResourceNotFoundException("Exercise with ID :: " + id + " not FOUND!!!!");
        }

        return getExercise.get();
    }

    @Override
    public int getExerciseCount() {
        return exerciseCatalog.size();
    }

}
