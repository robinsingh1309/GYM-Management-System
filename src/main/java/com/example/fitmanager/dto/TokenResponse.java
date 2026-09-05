package com.example.fitmanager.dto;

public class TokenResponse {

    // Fields

    private String message;

    private String token;


    // Constructors
    // --------------------------------------------------------

    public TokenResponse() {}

    public TokenResponse(final String message, final String token) {
        this.message = message;
        this.token = token;
    }


    // Getters & Setters
    // --------------------------------------------------------

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

}
