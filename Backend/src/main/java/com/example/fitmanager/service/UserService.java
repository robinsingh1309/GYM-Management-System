package com.example.fitmanager.service;

import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;


public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

}
