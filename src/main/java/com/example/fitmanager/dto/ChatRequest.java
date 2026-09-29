package com.example.fitmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class ChatRequest {

    // Fields

    @NotBlank(message = "Question is required")
    @Size(max = 1000, message = "Question must not exceed 1000 characters")
    private String question;


    // Constructors
    // ---------------------------------------------------------

    public ChatRequest() {
        //
    }


    // Getters and Setters
    // ---------------------------------------------------------

    public String getQuestion() {
        return question;
    }

    public void setQuestion(final String question) {
        this.question = question;
    }
}
