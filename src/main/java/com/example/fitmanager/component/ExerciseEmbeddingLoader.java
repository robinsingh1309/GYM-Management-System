package com.example.fitmanager.component;

import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.EmbeddingService;
import com.example.fitmanager.service.ExerciseService;


@Component
public class ExerciseEmbeddingLoader implements SmartInitializingSingleton {

    // Fields

    private final ExerciseService exerciseService;

    private final EmbeddingService embeddingService;
    private final ExerciseEmbeddingStore embeddingStore;


    // Constructors
    // ---------------------------------------------------------------------

    @Autowired
    public ExerciseEmbeddingLoader( //
            final ExerciseService exerciseService, //
            final EmbeddingService embeddingService, //
            final ExerciseEmbeddingStore embeddingStore) {

        this.exerciseService = exerciseService;

        this.embeddingService = embeddingService;
        this.embeddingStore = embeddingStore;
    }


    // Methods
    // ---------------------------------------------------------------------

    @Override
    public void afterSingletonsInstantiated() {

        embeddingStore.clear();

        final List<Exercise> exercises = exerciseService.getAllExercises();
        for (final Exercise exercise : exercises) {

            final float[] embedding = embeddingService.generateEmbedding( //
                    exercise.getEmbeddingText() //
            );
            
            embeddingStore.put(exercise.getId(), embedding);
        }
    }
}
