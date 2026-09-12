package com.example.fitmanager.exception;


@SuppressWarnings("serial")
public class BadRequestException extends RuntimeException {

    // Constructors
    // ----------------------------------------------------------

    public BadRequestException(final String message) {
        super(message);
    }
}
