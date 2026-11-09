package com.example.fitmanager.serviceImpl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.fitmanager.service.EmbeddingService;

import tools.jackson.databind.JsonNode;


@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    // Fields

    private final RestClient embeddingRestClient;
    private final String embeddingModel;
    private final int vectorDimension;


    // Constructors
    // ---------------------------------------------------------

    public EmbeddingServiceImpl( //
            final @Qualifier("embeddingRestClient") RestClient embeddingRestClient, //
            final @Value("${embedding.model}") String embeddingModel, //
            final @Value("${embedding.vector-dimension}") int vectorDimension) {

        if (embeddingModel.isBlank()) {
            throw new IllegalArgumentException("Embedding model cannot be blank");
        }
        if (vectorDimension <= 0) {
            throw new IllegalArgumentException("Embedding vector dimension must be greater than zero");
        }

        this.embeddingRestClient = embeddingRestClient;
        this.embeddingModel = embeddingModel;
        this.vectorDimension = vectorDimension;
    }


    // Methods
    // ---------------------------------------------------------

    @Override
    public float[] generateEmbedding(final String text) {

        if (Objects.isNull(text) || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank");
        }

        final EmbeddingRequest request = new EmbeddingRequest(embeddingModel, text);

        try {

            final JsonNode response = embeddingRestClient.post() //
                    .uri("/api/embed") //
                    .body(request) //
                    .retrieve() //
                    .body(JsonNode.class);

            if (Objects.isNull(response) || response.isEmpty()) {
                throw new IllegalStateException("Ollama returned an empty embedding response");
            }

            return this.extractEmbedding(response);

        } catch (RestClientException exception) {
            throw new IllegalStateException("Failed to generate embedding using Ollama", exception);
        }
    }

    private float[] extractEmbedding(final JsonNode response) {

        final JsonNode embeddings = response.get("embeddings");
        if (Objects.isNull(embeddings) || !embeddings.isArray() || embeddings.isEmpty()) {
            throw new IllegalStateException("Unexpected Ollama embedding response");
        }

        final JsonNode embeddingNode = embeddings.get(0);
        if (!embeddingNode.isArray() || embeddingNode.size() != vectorDimension) {
            throw new IllegalStateException("Unexpected Ollama embedding vector dimension");
        }

        return this.toFloatArray(embeddingNode);
    }

    private float[] toFloatArray(final JsonNode node) {

        final float[] embedding = new float[node.size()];
        for (int i = 0; i < node.size(); i++) {
            if (!node.get(i).isNumber()) {
                throw new IllegalStateException("Unexpected Ollama embedding value");
            }
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

    private record EmbeddingRequest(String model, String input) {
    }
}
