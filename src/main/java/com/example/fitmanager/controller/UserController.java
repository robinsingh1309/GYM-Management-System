package com.example.fitmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fitmanager.constant.AuthProviderConstants;
import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // Fields

    private final UserService userService;


    // Constructors
    // ---------------------------------------------------

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }


    // End Points
    // ---------------------------------------------------

    @PostMapping
    public ResponseEntity<UserResponse> createUser( //
            @Valid @RequestBody final UserCreateRequest request) {

        request.setProvider(AuthProviderConstants.LOCAL_PROVIDER);
        request.setProviderSubject(null);

        final UserResponse response = userService.createUser(request);

        return ResponseEntity //
                .status(HttpStatus.CREATED) //
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") final Long id) {

        final UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
