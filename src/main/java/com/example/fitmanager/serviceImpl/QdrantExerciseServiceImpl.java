package com.example.fitmanager.serviceImpl;

import static io.qdrant.client.ConditionFactory.match;
import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.fitmanager.component.ExerciseEmbeddingStore;
import com.example.fitmanager.dto.ExerciseRecommendationRequest;
import com.example.fitmanager.dto.ExerciseRecommendationResponse;
import com.example.fitmanager.dto.QdrantExerciseMatch;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.EmbeddingService;
import com.example.fitmanager.service.ExerciseRecommendationQueryService;
import com.example.fitmanager.service.ExerciseService;
import com.example.fitmanager.service.QdrantExerciseService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;


@Service
public class QdrantExerciseServiceImpl implements QdrantExerciseService {

    // Fields

    private final QdrantClient qdrantClient;
    private final ExerciseService exerciseService;
    private final EmbeddingService embeddingService;
    private final ExerciseRecommendationQueryService queryService;
    private final ExerciseEmbeddingStore embeddingStore;
    private final String collectionName;


    // Constructors
    // ------------------------------------------------------------------

    public QdrantExerciseServiceImpl( //
            final QdrantClient qdrantClient, //
            final ExerciseService exerciseService, //
            final EmbeddingService embeddingService, //
            final ExerciseRecommendationQueryService queryService, //
            final ExerciseEmbeddingStore embeddingStore, //
            final @Value("${qdrant.collection}") String collectionName) {

        this.qdrantClient = qdrantClient;
        this.exerciseService = exerciseService;
        this.embeddingService = embeddingService;
        this.queryService = queryService;
        this.embeddingStore = embeddingStore;
        this.collectionName = collectionName;
    }


    // Methods
    // ------------------------------------------------------------------

    @Override
    public boolean collectionExists() {

        try {
            return qdrantClient //
                    .collectionExistsAsync(collectionName) //
                    .get();
        } catch (Exception exception) {
            throw new IllegalStateException( //
                    "Failed to check Qdrant collection", exception);
        }
    }

    @Override
    public long getPointCount() {

        try {
            return qdrantClient //
                    .countAsync(collectionName) //
                    .get();
        } catch (Exception exception) {
            throw new IllegalStateException( //
                    "Failed to count Qdrant points", exception);
        }
    }

    @Override
    public Map<String, Object> getExercisePoint(final Long exerciseId) {

        final Exercise exercise = exerciseService.getExerciseById(exerciseId);
        final float[] embedding = this.getEmbedding(exerciseId);

        final Map<String, Object> point = Map.of( //
                "id", exercise.getId(), //
                "vector", embedding, //
                "payload", this.createExercisePayload(exercise) //
        );

        return Map.of("points", List.of(point));
    }

    @Override
    public void upsertExercise(final Long exerciseId) {

        final Exercise exercise = exerciseService.getExerciseById(exerciseId);

        final float[] embedding = this.getEmbedding(exerciseId);

        try {
            final PointStruct point = this.createPoint(exercise, embedding);
            qdrantClient.upsertAsync(collectionName, List.of(point)).get();

        } catch (Exception exception) {
            throw new IllegalStateException( //
                    "Failed to upsert exercise into Qdrant. Exercise id: " + exerciseId, exception);
        }
    }

    @Override
    public int syncAllExercises() {

        final List<Exercise> exercises = exerciseService.getAllExercises();
        final int exercisesListSize = exercises.size();

        final int batchSize = 100;
        int syncedCount = 0;

        try {
            for (int start = 0; start < exercisesListSize; start += batchSize) {

                final int endIndex = Math.min(start + batchSize, exercisesListSize);

                final List<Exercise> exerciseBatch = exercises.subList(start, endIndex);
                final int exerciseBatchSize = exerciseBatch.size();

                final List<PointStruct> points = new ArrayList<>(exerciseBatchSize);

                for (final Exercise exercise : exerciseBatch) {

                    final float[] embedding = this.getEmbedding(exercise.getId());
                    points.add(this.createPoint(exercise, embedding));
                }

                qdrantClient //
                        .upsertAsync(collectionName, points) //
                        .get();

                syncedCount += exerciseBatchSize;
            }

            return syncedCount;
        } catch (final Exception exception) {
            throw new IllegalStateException( //
                    "Failed to synchronize exercises with Qdrant", exception);
        }
    }

    @Override
    public List<ExerciseRecommendationResponse> recommend( //
            final ExerciseRecommendationRequest request) {

        if (Objects.isNull(request.getQuery()) || request.getQuery().isBlank()) {
            throw new IllegalArgumentException("Recommendation query cannot be blank");
        }

        queryService.populateMissingFilters(request);

        final float[] queryEmbedding = embeddingService.generateEmbedding(request.getQuery());

        final List<ExerciseRecommendationResponse> exerciseRecommendations = //
                this.search(queryEmbedding, request).stream() //
                        .map(match -> new ExerciseRecommendationResponse( //
                                exerciseService.getExerciseById(match.exerciseId()), //
                                match.score() //
                        )) //
                        .toList();

        return exerciseRecommendations;
    }


    // Private Methods
    // ------------------------------------------------------------------

    private List<QdrantExerciseMatch> search(final float[] queryEmbedding, //
            final ExerciseRecommendationRequest request) {

        if (Objects.isNull(queryEmbedding) || queryEmbedding.length == 0) {
            throw new IllegalArgumentException("Query embedding cannot be empty");
        }

        final int limit = Objects.isNull(request.getLimit()) ? 5 : request.getLimit();
        if (limit <= 0) {
            throw new IllegalArgumentException("Search limit must be greater than zero");
        }

        final Filter filter = this.buildFilter(request);
        try {
            final List<ScoredPoint> searchResult = //
                    qdrantClient //
                            .queryAsync( //
                                    QueryPoints.newBuilder().setCollectionName(collectionName).setLimit(limit) //
                                            .setQuery(nearest(queryEmbedding)) //
                                            .setFilter(filter) //
                                            .build() //
                            ).get();

            return searchResult.stream() //
                    .map( //
                            point -> new QdrantExerciseMatch( //
                                    point.getId().getNum(), //
                                    point.getScore() //
                            ) //
                    ).toList();

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to search exercises in Qdrant", exception);
        }
    }


    private List<Float> toFloatList(final float[] embedding) {

        final int embeddingLength = embedding.length;
        final List<Float> values = new ArrayList<>(embeddingLength);

        for (final float value : embedding) {
            values.add(value);
        }

        return values;
    }

    private Map<String, JsonWithInt.Value> createPayload(final Exercise exercise) {

        return Map.of( //
                "name", value(exercise.getName()), //

                "bodyPart", value(exercise.getBodyPart()), //
                "primaryMuscle", value(exercise.getPrimaryMuscle()), //

                "difficulty", value(exercise.getDifficulty()), //

                "equipment", value( //
                        exercise.getEquipment().stream() //
                                .map(item -> value(item)) //
                                .toList() //
                ), //

                "goalTags", value( //
                        exercise.getGoalTags().stream() //
                                .map(item -> value(item)) //
                                .toList() //
                ), //

                "active", value(exercise.isActive()) //
        );
    }

    private Map<String, Object> createExercisePayload(final Exercise exercise) {

        return Map.of( //
                "name", exercise.getName(), //
                "bodyPart", exercise.getBodyPart(), //
                "primaryMuscle", exercise.getPrimaryMuscle(), //
                "difficulty", exercise.getDifficulty(), //
                "equipment", exercise.getEquipment(), //
                "goalTags", exercise.getGoalTags(), //
                "active", exercise.isActive() //
        );
    }

    private float[] getEmbedding(final Long exerciseId) {

        final float[] embedding = embeddingStore.get(exerciseId);
        if (Objects.isNull(embedding)) {
            throw new IllegalStateException( //
                    "Embedding not found for exercise id: " + exerciseId);
        }

        return embedding;
    }

    private PointStruct createPoint(final Exercise exercise, final float[] embedding) {

        return PointStruct.newBuilder() //
                .setId( //
                        id(exercise.getId()) //
                ) //
                .setVectors( //
                        vectors(this.toFloatList(embedding)) //
                ) //
                .putAllPayload( //
                        this.createPayload(exercise) //
                ) //
                .build();
    }

    private Filter buildFilter(final ExerciseRecommendationRequest request) {

        final Filter.Builder filterBuilder = Filter.newBuilder();

        // Mandatory filter
        filterBuilder.addMust(match("active", true));

        // Optional body-part filter
        if (Objects.nonNull(request.getBodyPart()) && !request.getBodyPart().isBlank()) {
            filterBuilder.addMust(matchKeyword("bodyPart", request.getBodyPart()));
        }

        // Optional difficulty filter
        if (Objects.nonNull(request.getDifficulty()) && !request.getDifficulty().isBlank()) {
            filterBuilder.addMust(matchKeyword("difficulty", request.getDifficulty()));
        }

        // Optional goalTags filter
        if (Objects.nonNull(request.getGoal()) && !request.getGoal().isBlank()) {
            filterBuilder.addMust(matchKeyword("goalTags", request.getGoal()));
        }

        // Optional equipment filter
        if (Objects.nonNull(request.getEquipment()) && !request.getEquipment().isEmpty()) {
            for (final String equipment : request.getEquipment()) {
                if (Objects.nonNull(equipment) && !equipment.isBlank()) {
                    filterBuilder.addMust(matchKeyword("equipment", equipment));
                }
            }
        }

        return filterBuilder.build();
    }
}
