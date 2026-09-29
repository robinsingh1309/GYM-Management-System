package com.example.fitmanager.serviceImpl;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.KnowledgeIngestionResponse;
import com.example.fitmanager.service.EmbeddingService;
import com.example.fitmanager.service.KnowledgeIngestionService;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points.PointStruct;


@Service
public class KnowledgeIngestionServiceImpl implements KnowledgeIngestionService {

    // Fields

    private static final String KNOWLEDGE_RESOURCE_PATTERN = "classpath:knowledgebase/*.pdf";

    private final QdrantClient qdrantClient;
    private final EmbeddingService embeddingService;
    private final ResourcePatternResolver resourceResolver;
    private final String collectionName;
    private final int vectorDimension;
    private final int batchSize;


    // Constructors
    // ---------------------------------------------------------

    public KnowledgeIngestionServiceImpl( //
            final QdrantClient qdrantClient, //
            final EmbeddingService embeddingService, //
            @Value("${qdrant.knowledgebase-collection}") final String collectionName, //
            @Value("${rag.knowledgebase.vector-dimension:384}") final int vectorDimension, //
            @Value("${rag.knowledgebase.batch-size:100}") final int batchSize) {

        if (collectionName.isBlank()) {
            throw new IllegalArgumentException("Knowledge collection name cannot be blank");
        }
        if (vectorDimension <= 0 || batchSize <= 0) {
            throw new IllegalArgumentException("Knowledge indexing configuration values must be greater than zero");
        }

        this.qdrantClient = qdrantClient;
        this.embeddingService = embeddingService;
        this.resourceResolver = new PathMatchingResourcePatternResolver();
        this.collectionName = collectionName;
        this.vectorDimension = vectorDimension;
        this.batchSize = batchSize;
    }


    // Methods
    // ---------------------------------------------------------

    @Override
    public KnowledgeIngestionResponse indexKnowledgeBase() {

        try {
            final Resource[] resources = resourceResolver.getResources(KNOWLEDGE_RESOURCE_PATTERN);
            final List<Resource> documents = List.of(resources).stream() //
                    .sorted(Comparator.comparing(Resource::getFilename)) //
                    .toList();

            if (documents.isEmpty()) {
                throw new IllegalStateException("No knowledge-base PDF documents were found");
            }

            final List<PointStruct> points = new ArrayList<>();
            for (final Resource document : documents) {
                points.addAll(this.createPoints(document));
            }

            if (points.isEmpty()) {
                throw new IllegalStateException("Knowledge-base documents produced no indexable content");
            }

            qdrantClient.deleteAsync(collectionName, Filter.getDefaultInstance()).get();

            for (int start = 0; start < points.size(); start += batchSize) {
                final int end = Math.min(start + batchSize, points.size());
                qdrantClient.upsertAsync(collectionName, points.subList(start, end)).get();
            }

            return new KnowledgeIngestionResponse(documents.size(), points.size());

        } catch (final IllegalStateException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new IllegalStateException("Failed to index the knowledge base", exception);
        }
    }


    // Private Methods
    // ---------------------------------------------------------

    private List<PointStruct> createPoints(final Resource resource) {

        final String sourceFile = resource.getFilename();
        if (Objects.isNull(sourceFile)) {
            throw new IllegalStateException("Knowledge-base resource has no filename");
        }

        final List<Document> documents = new PagePdfDocumentReader(resource).read();
        final List<PointStruct> points = new ArrayList<>();
        for (int pageIndex = 0; pageIndex < documents.size(); pageIndex++) {
            final String content = documents.get(pageIndex).getText();
            if (Objects.isNull(content) || content.isBlank()) {
                continue;
            }

            final float[] embedding = embeddingService.generateEmbedding(content);
            if (Objects.isNull(embedding) || embedding.length != vectorDimension) {
                throw new IllegalStateException("Unexpected embedding dimension for " + sourceFile);
            }

            final int pageNumber = pageIndex + 1;
            final String pointKey = sourceFile + "|" + pageNumber;
            points.add(PointStruct.newBuilder() //
                    .setId(id(UUID.nameUUIDFromBytes(pointKey.getBytes(StandardCharsets.UTF_8)))) //
                    .setVectors(vectors(this.toFloatList(embedding))) //
                    .putAllPayload(this.createPayload(sourceFile, pageNumber, content)) //
                    .build());
        }

        return points;
    }

    private List<Float> toFloatList(final float[] embedding) {

        final List<Float> values = new ArrayList<>(embedding.length);
        for (final float value : embedding) {
            values.add(value);
        }
        return values;
    }

    private Map<String, JsonWithInt.Value> createPayload(final String sourceFile, //
            final int pageNumber, final String content) {

        return Map.of( //
                "content", value(content), //
                "sourceFile", value(sourceFile), //
                "pageNumber", value(pageNumber), //
                "contentHash", value(this.sha256(content)) //
        );
    }

    private String sha256(final String content) {

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
