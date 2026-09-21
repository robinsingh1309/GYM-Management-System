package com.example.fitmanager.service;

import com.example.fitmanager.dto.AuthenticationRequest;
import com.example.fitmanager.dto.AuthenticationResponse;
import com.example.fitmanager.dto.TokenResponse;
import com.example.fitmanager.dto.UserResponse;


public interface AuthenticationService {

    AuthenticationResponse register(AuthenticationRequest request);

    UserResponse authenticateGoogleUser(String providerSubject, String email);

    TokenResponse exchangeOAuth2Code(String code);
}
