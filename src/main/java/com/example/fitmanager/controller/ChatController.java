package com.example.fitmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.ChatRequest;
import com.example.fitmanager.dto.ChatResponse;
import com.example.fitmanager.service.GroundedAnsweringService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    // Fields

    private final GroundedAnsweringService groundedAnsweringService;


    // Constructors
    // ---------------------------------------------------------

    @Autowired
    public ChatController(final GroundedAnsweringService groundedAnsweringService) {
        this.groundedAnsweringService = groundedAnsweringService;
    }


    // API End Points
    // ---------------------------------------------------------

    // POST

    @PostMapping
    public ResponseEntity<ChatResponse> chat( //
            @Valid @RequestBody final ChatRequest request) {

        return ResponseEntity.ok(groundedAnsweringService.answer(request.getQuestion()));
    }
}
