package com.example.fitmanager.dto;


public class KnowledgeIngestionResponse {

    // Fields

    private final int documentCount;
    private final int chunkCount;


    // Constructors
    // ------------------------------------------------------------

    public KnowledgeIngestionResponse(final int documentCount, final int chunkCount) {
        this.documentCount = documentCount;
        this.chunkCount = chunkCount;
    }


    // Getters
    // ------------------------------------------------------------

    public int getDocumentCount() {
        return documentCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }
}
