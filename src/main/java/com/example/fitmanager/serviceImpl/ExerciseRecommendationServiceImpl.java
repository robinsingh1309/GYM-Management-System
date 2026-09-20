package com.example.fitmanager.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.fitmanager.component.ExerciseEmbeddingStore;
import com.example.fitmanager.dto.ExerciseRecommendationResponse;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.EmbeddingService;
import com.example.fitmanager.service.ExerciseRecommendationService;
import com.example.fitmanager.service.ExerciseService;
import com.example.fitmanager.util.VectorSimilarityUtil;


@Service
public class ExerciseRecommendationServiceImpl implements ExerciseRecommendationService {

    // Fields

    private final ExerciseService exerciseService;

    private final EmbeddingService embeddingService;
    private final ExerciseEmbeddingStore embeddingStore;


    // Constructors
    // -------------------------------------------------------

    public ExerciseRecommendationServiceImpl( //
            final ExerciseService exerciseService, //
            EmbeddingService embeddingService, ExerciseEmbeddingStore embeddingStore) {

        this.exerciseService = exerciseService;

        this.embeddingService = embeddingService;
        this.embeddingStore = embeddingStore;
    }


    // Methods
    // -------------------------------------------------------

    @Override
    public List<ExerciseRecommendationResponse> recommend(final String query, final int limit) {

        if (Objects.isNull(query) || query.isBlank()) {
            throw new IllegalArgumentException("Recommendation query cannot be blank");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException("Recommendation limit must be greater than zero");
        }

        final float[] queryEmbedding = embeddingService.generateEmbedding(query);

        return exerciseService.getAllExercises().stream() //
                .filter(Exercise::isActive) //
                .filter( //
                        exercise -> embeddingStore.contains(exercise.getId()) //
                ).map(exercise -> { //

                    float[] exerciseEmbedding = embeddingStore.get(exercise.getId()); //
                    double similarity = VectorSimilarityUtil.cosineSimilarity(queryEmbedding, exerciseEmbedding); //

                    return new ExerciseRecommendationResponse(exercise, similarity);
                }) //
                .sorted( //
                        Comparator.comparingDouble( //
                                ExerciseRecommendationResponse::getSimilarityScore) //
                                .reversed() //
                ) //
                .limit(limit) //
                .toList();
    }
}
