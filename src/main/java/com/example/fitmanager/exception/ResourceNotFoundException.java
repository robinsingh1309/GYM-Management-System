package com.example.fitmanager.exception;


@SuppressWarnings("serial")
public class ResourceNotFoundException extends RuntimeException {

    // Constructors
    // ----------------------------------------------------------

    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
