package com.example.fitmanager.service;

import java.util.Optional;

import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;


public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    Optional<UserResponse> findUser(String provider, String providerSubject);

    Optional<UserResponse> findUserByEmail(String email);

}
