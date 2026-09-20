package com.example.fitmanager.serviceImpl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.fitmanager.service.EmbeddingService;

import tools.jackson.databind.JsonNode;


@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    // Fields

    private final RestClient huggingFaceRestClient;
    private final String embeddingModel;


    // Constructors
    // ---------------------------------------------------------

    public EmbeddingServiceImpl( //
            final RestClient huggingFaceRestClient, //
            @Value("${huggingface.embedding.model}") String embeddingModel) {

        this.huggingFaceRestClient = huggingFaceRestClient;
        this.embeddingModel = embeddingModel;
    }


    // Methods
    // ---------------------------------------------------------

    @Override
    public float[] generateEmbedding(final String text) {

        if (Objects.isNull(text) || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank");
        }

        final EmbeddingRequest request = new EmbeddingRequest(List.of(text));

        try {

            final JsonNode response = huggingFaceRestClient.post() //
                    .uri("/" + embeddingModel + "/pipeline/feature-extraction") //
                    .body(request) //
                    .retrieve() //
                    .body(JsonNode.class);

            if (Objects.isNull(response) || response.isEmpty()) {
                throw new IllegalStateException("Hugging Face returned an empty embedding response");
            }

            return this.extractSentenceEmbedding(response);

        } catch (RestClientResponseException exception) {
            throw new IllegalStateException("Failed to generate embedding. Exception :: + ", exception);
        }
    }

    private float[] extractSentenceEmbedding(final JsonNode response) {

        if (!response.isArray() || response.isEmpty()) {
            throw new IllegalStateException("Unexpected Hugging Face embedding response");
        }

        final JsonNode firstBatch = response.get(0);
        if (!firstBatch.isArray() || firstBatch.isEmpty()) {
            throw new IllegalStateException("Unexpected Hugging Face embedding response");
        }

        if (firstBatch.get(0).isNumber()) {
            return toFloatArray(firstBatch);
        }

        int tokenCount = firstBatch.size();

        final JsonNode firstToken = firstBatch.get(0);
        if (!firstToken.isArray()) {
            throw new IllegalStateException("Unexpected token embedding format");
        }

        final int dimensions = firstToken.size();
        final float[] pooledEmbedding = new float[dimensions];

        for (final JsonNode tokenEmbedding : firstBatch) {
            for (int i = 0; i < dimensions; i++) {
                pooledEmbedding[i] += tokenEmbedding.get(i).floatValue();
            }
        }

        for (int i = 0; i < dimensions; i++) {
            pooledEmbedding[i] /= tokenCount;
        }

        this.normalize(pooledEmbedding);
        return pooledEmbedding;
    }

    private float[] toFloatArray(final JsonNode node) {

        final float[] embedding = new float[node.size()];
        for (int i = 0; i < node.size(); i++) {
            embedding[i] = node.get(i).floatValue();
        }

        this.normalize(embedding);
        return embedding;
    }

    private void normalize(final float[] vector) {

        double sumOfSquares = 0.0;
        for (final float value : vector) {
            sumOfSquares += value * value;
        }

        final double magnitude = Math.sqrt(sumOfSquares);
        if (magnitude == 0.0) {
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / magnitude);
        }
    }

    private record EmbeddingRequest(List<String> inputs) {
    }
}
