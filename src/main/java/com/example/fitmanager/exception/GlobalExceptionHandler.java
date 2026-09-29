package com.example.fitmanager.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // Resource NOT FOUND!!!
    // ------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException( //
            final ResourceNotFoundException ex, final HttpServletRequest req) {

        final ErrorResponse response = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), //
                HttpStatus.NOT_FOUND.getReasonPhrase(), //
                ex.getMessage(), req.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Bad Request
    // ------------------------------------------------------------------

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException( //
            final BadRequestException ex, final HttpServletRequest req) {

        final ErrorResponse response = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), //
                HttpStatus.BAD_REQUEST.getReasonPhrase(), //
                ex.getMessage(), req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Field Validation
    // ------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException( //
            final MethodArgumentNotValidException ex, final HttpServletRequest req) {

        final String message = ex.getBindingResult() //
                .getFieldErrors() //
                .stream() //
                .map(error -> error.getField() + ": " + error.getDefaultMessage())//
                .collect(Collectors.joining(", "));

        final ErrorResponse response = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), //
                HttpStatus.BAD_REQUEST.getReasonPhrase(), message, req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Method Argument Type
    // ------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(//
            final MethodArgumentTypeMismatchException ex, final HttpServletRequest req) {

        final String message = "Invalid value for parameter: " + ex.getName();

        final ErrorResponse response = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), //
                HttpStatus.BAD_REQUEST.getReasonPhrase(), message, req.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Generic
    // ------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(//
            final Exception ex, final HttpServletRequest req) {

        final ErrorResponse response = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), //
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "An unexpected error occurred", //
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // HTTP Message Not Readable
    // ------------------------------------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(final HttpMessageNotReadableException exception,
            final HttpServletRequest request) {

        final ErrorResponse errorResponse = new ErrorResponse( //
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), //
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request body", //
                request.getRequestURI() //
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Authentication
    // ------------------------------------------------------------------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException( //
            final BadCredentialsException ex, final HttpServletRequest req) {

        final ErrorResponse response = new ErrorResponse(LocalDateTime.now(), //
                HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), //
                "Invalid email or password", req.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
