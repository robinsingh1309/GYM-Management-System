package com.example.fitmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.KnowledgeIngestionResponse;
import com.example.fitmanager.service.KnowledgeIngestionService;


@RestController
@RequestMapping("/api/v1/knowledge-base")
public class KnowledgeIngestionController {

    // Fields

    private final KnowledgeIngestionService knowledgeIngestionService;


    // Constructors
    // ---------------------------------------------------------

    @Autowired
    public KnowledgeIngestionController(final KnowledgeIngestionService knowledgeIngestionService) {
        this.knowledgeIngestionService = knowledgeIngestionService;
    }


    // API End Points
    // ---------------------------------------------------------

    // POST

    @PostMapping("/reindex")
    public ResponseEntity<KnowledgeIngestionResponse> reindexKnowledgeBase() {
        return ResponseEntity.ok(knowledgeIngestionService.indexKnowledgeBase());
    }
}
