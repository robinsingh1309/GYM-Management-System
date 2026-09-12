package com.example.fitmanager.service;

import com.example.fitmanager.dto.AuthenticationRequest;
import com.example.fitmanager.dto.AuthenticationResponse;


public interface AuthenticationService {

    AuthenticationResponse register(AuthenticationRequest request);
}
