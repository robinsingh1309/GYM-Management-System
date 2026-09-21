package com.example.fitmanager.dto;

import jakarta.validation.constraints.NotBlank;


public class OAuth2ExchangeRequest {

    // Fields

    @NotBlank(message = "OAuth2 code is required")
    private String code;


    // Constructors
    // -------------------------------------------------------

    public OAuth2ExchangeRequest() {
        //
    }


    // Getters and Setters
    // -------------------------------------------------------

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }
}
