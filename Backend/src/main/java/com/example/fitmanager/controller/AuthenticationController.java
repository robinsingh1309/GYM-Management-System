package com.example.fitmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.dto.AuthenticationRequest;
import com.example.fitmanager.dto.AuthenticationResponse;
import com.example.fitmanager.dto.TokenResponse;
import com.example.fitmanager.security.JwtService;
import com.example.fitmanager.service.AuthenticationService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    // Fields

    private final AuthenticationManager authenticationManager;

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;


    // Constructors
    // -------------------------------------------------------

    @Autowired
    public AuthenticationController(final AuthenticationManager authenticationManager, //
            final AuthenticationService authenticationService, final JwtService jwtService) {

        this.authenticationManager = authenticationManager;

        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }


    // End Points
    // -------------------------------------------------------

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register( //
            @Valid @RequestBody final AuthenticationRequest request) {

        final AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthenticationRequest request) {

        final Authentication authenticationRequest = //
                UsernamePasswordAuthenticationToken.unauthenticated(request.getEmail(), request.getPassword());

        final Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        final TokenResponse loginResponse = jwtService.generateToken(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }
}
