package com.example.fitmanager.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.ExerciseRecommendationRequest;
import com.example.fitmanager.entity.Exercise;
import com.example.fitmanager.service.ExerciseRecommendationQueryService;
import com.example.fitmanager.service.ExerciseService;


@Service
public class ExerciseRecommendationQueryServiceImpl implements ExerciseRecommendationQueryService {

    // Fields

    private final ExerciseService exerciseService;


    // Constructors
    // ------------------------------------------------------------

    public ExerciseRecommendationQueryServiceImpl(final ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }


    // Methods
    // ------------------------------------------------------------

    @Override
    public void populateMissingFilters(final ExerciseRecommendationRequest request) {

        if (Objects.isNull(request) || Objects.isNull(request.getQuery()) || request.getQuery().isBlank()) {
            return;
        }

        final List<Exercise> exercises = exerciseService.getAllExercises();
        if (Objects.isNull(exercises) || exercises.isEmpty()) {
            return;
        }

        final String normalizedQuery = request.getQuery().toLowerCase(Locale.ROOT);

        if (this.isBlank(request.getBodyPart())) {
            request.setBodyPart( //
                    this.firstMatchingValue( //
                            normalizedQuery, //
                            exercises.stream().map(Exercise::getBodyPart) //
                    ) //
            );
        }

        if (this.isBlank(request.getDifficulty())) {
            request.setDifficulty( //
                    this.firstMatchingValue( //
                            normalizedQuery, //
                            exercises.stream().map(Exercise::getDifficulty) //
                    ) //
            );
        }

        if (Objects.isNull(request.getEquipment()) || request.getEquipment().isEmpty()) {
            final List<String> equipment = this.matchingValues( //
                    normalizedQuery, //
                    exercises.stream() //
                            .map(Exercise::getEquipment) //
                            .filter(Objects::nonNull) //
                            .flatMap(List::stream) //
            );

            if (!equipment.isEmpty()) {
                request.setEquipment(equipment);
            }
        }

        if (this.isBlank(request.getGoal())) {
            request.setGoal( //
                    this.firstMatchingValue( //
                            normalizedQuery, //
                            exercises.stream() //
                                    .map(Exercise::getGoalTags) //
                                    .filter(Objects::nonNull) //
                                    .flatMap(List::stream) //
                    ) //
            );
        }
    }


    // Private Methods
    // ------------------------------------------------------------

    private String firstMatchingValue(final String normalizedQuery, //
            final Stream<String> values) {

        return this.matchingValues(normalizedQuery, values).stream() //
                .findFirst() //
                .orElse(null);
    }

    private List<String> matchingValues(final String normalizedQuery, //
            final Stream<String> values) {

        return values //
                .filter(Objects::nonNull) //
                .filter(value -> !value.isBlank()) //
                .distinct() //
                .sorted(Comparator.comparingInt(String::length).reversed()) //
                .filter(value -> this.containsCompleteTerm(normalizedQuery, value)) //
                .toList();
    }

    private boolean containsCompleteTerm(final String normalizedQuery, final String value) {

        final String normalizedValue = value.toLowerCase(Locale.ROOT);
        final Pattern pattern = Pattern.compile( //
                "(?<![\\p{L}\\p{N}])" + Pattern.quote(normalizedValue) + "(?![\\p{L}\\p{N}])" //
        );

        return pattern.matcher(normalizedQuery).find();
    }

    private boolean isBlank(final String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
