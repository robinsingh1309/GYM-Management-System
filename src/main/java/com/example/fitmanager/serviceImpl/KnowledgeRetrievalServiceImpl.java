package com.example.fitmanager.serviceImpl;

import static io.qdrant.client.QueryFactory.nearest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.KnowledgeMatch;
import com.example.fitmanager.service.EmbeddingService;
import com.example.fitmanager.service.KnowledgeRetrievalService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.QueryPoints;
import io.qdrant.client.grpc.Points.ScoredPoint;


@Service
public class KnowledgeRetrievalServiceImpl implements KnowledgeRetrievalService {

    // Fields

    private final QdrantClient qdrantClient;
    private final EmbeddingService embeddingService;
    private final String collectionName;
    private final int vectorDimension;
    private final int resultLimit;
    private final float minimumScore;


    // Constructors
    // ---------------------------------------------------------

    public KnowledgeRetrievalServiceImpl( //
            final QdrantClient qdrantClient, //
            final EmbeddingService embeddingService, //
            @Value("${qdrant.knowledgebase-collection}") final String collectionName, //
            @Value("${rag.knowledgebase.vector-dimension}") final int vectorDimension, //
            @Value("${rag.knowledgebase.search-limit}") final int resultLimit, //
            @Value("${rag.knowledgebase.minimum-score}") final float minimumScore) {

        if (collectionName.isBlank()) {
            throw new IllegalArgumentException("Knowledge collection name cannot be blank");
        }
        if (vectorDimension <= 0 || resultLimit <= 0) {
            throw new IllegalArgumentException("Knowledge retrieval configuration values must be greater than zero");
        }
        if (minimumScore < -1.0f || minimumScore > 1.0f) {
            throw new IllegalArgumentException("Knowledge retrieval minimum score must be between -1 and 1");
        }

        this.qdrantClient = qdrantClient;
        this.embeddingService = embeddingService;
        this.collectionName = collectionName;
        this.vectorDimension = vectorDimension;
        this.resultLimit = resultLimit;
        this.minimumScore = minimumScore;
    }


    // Methods
    // ---------------------------------------------------------

    @Override
    public List<KnowledgeMatch> retrieve(final String question) {

        if (Objects.isNull(question) || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be blank");
        }

        final float[] queryEmbedding = embeddingService.generateEmbedding(question);
        if (Objects.isNull(queryEmbedding) || queryEmbedding.length != vectorDimension) {
            throw new IllegalStateException("Unexpected knowledge-query embedding dimension");
        }

        try {
            final QueryPoints query = QueryPoints.newBuilder() //
                    .setCollectionName(collectionName) //
                    .setQuery(nearest(queryEmbedding)) //
                    .setLimit(resultLimit) //
                    .setScoreThreshold(minimumScore) //
                    .setWithPayload(WithPayloadSelectorFactory.enable(true)) //
                    .build();

            return qdrantClient.queryAsync(query).get().stream() //
                    .map(this::toKnowledgeMatch) //
                    .toList();

        } catch (final Exception exception) {
            throw new IllegalStateException("Failed to retrieve knowledge from Qdrant", exception);
        }
    }


    // Private Methods
    // ---------------------------------------------------------

    private KnowledgeMatch toKnowledgeMatch(final ScoredPoint point) {

        final Map<String, JsonWithInt.Value> payload = point.getPayloadMap();
        final JsonWithInt.Value contentValue = payload.get("content");
        final JsonWithInt.Value sourceFileValue = payload.get("sourceFile");
        final JsonWithInt.Value pageNumberValue = payload.get("pageNumber");

        if (Objects.isNull(contentValue) || contentValue.getStringValue().isBlank() //
                || Objects.isNull(sourceFileValue) || sourceFileValue.getStringValue().isBlank() //
                || Objects.isNull(pageNumberValue) || pageNumberValue.getIntegerValue() <= 0) {
            throw new IllegalStateException("Knowledge point contains invalid payload metadata");
        }

        return new KnowledgeMatch( //
                contentValue.getStringValue(), //
                sourceFileValue.getStringValue(), //
                Math.toIntExact(pageNumberValue.getIntegerValue()), //
                point.getScore() //
        );
    }
}
