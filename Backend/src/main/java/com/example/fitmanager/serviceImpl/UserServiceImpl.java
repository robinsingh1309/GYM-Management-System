package com.example.fitmanager.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.entity.Role;
import com.example.fitmanager.entity.User;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.exception.ResourceNotFoundException;
import com.example.fitmanager.repository.UserRepository;
import com.example.fitmanager.service.UserService;


@Service
public class UserServiceImpl implements UserService {

    // Fields

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;


    // Constructors
    // ------------------------------------------------------

    @Autowired
    public UserServiceImpl(final UserRepository repository, //
            final PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }


    // Methods
    // ------------------------------------------------------

    @Override
    public UserResponse createUser(final UserCreateRequest request) {

        final String userEmail = request.getEmail();

        final String rawPassword = request.getPassword();
        final String encodedPassword = passwordEncoder.encode(rawPassword);

        final Role userRole = request.getRole();

        if (repository.existsByEmail(userEmail)) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        final User user = new User(userEmail, encodedPassword, userRole);

        final User savedUser = repository.save(user);
        return toDTO(savedUser);
    }

    @Override
    public UserResponse getUserById(final Long id) {
        final User user = repository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("User not found with id: " + id));

        return toDTO(user);
    }


    // Helper Methods
    // ------------------------------------------------------

    private UserResponse toDTO(final User user) {

        return new UserResponse(user.getId(), user.getEmail(), //
                user.getRole(), user.getActive(), user.getCreatedAt(), //
                user.getUpdatedAt());
    }

}
