package com.example.fitmanager.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.AuthenticationRequest;
import com.example.fitmanager.dto.AuthenticationResponse;
import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.entity.Role;
import com.example.fitmanager.service.AuthenticationService;
import com.example.fitmanager.service.UserService;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    // Fields

    private final UserService userService;


    // Constructors
    // -------------------------------------------------------------------

    @Autowired
    public AuthenticationServiceImpl(final UserService userService) {
        this.userService = userService;
    }


    // Methods
    // -------------------------------------------------------------------

    public AuthenticationResponse register(final AuthenticationRequest request) {

        final String userEmail = request.getEmail();
        final String userPassword = request.getPassword();

        final UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setEmail(userEmail);
        userCreateRequest.setPassword(userPassword);
        userCreateRequest.setRole(Role.STAFF);

        final UserResponse userResponse = userService.createUser(userCreateRequest);

        return new AuthenticationResponse(userResponse.getId(), //
                userResponse.getUserEmail(), userResponse.getRole());
    }

}
