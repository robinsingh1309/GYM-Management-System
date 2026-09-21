package com.example.fitmanager.serviceImpl;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.fitmanager.constant.AuthProviderConstants;
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
        final String provider = request.getProvider();
        final String providerSubject = request.getProviderSubject();

        final Role userRole = request.getRole();

        this.validateProviderCredentials(provider, providerSubject, rawPassword);

        final String encodedPassword = //
                Objects.isNull(rawPassword) //
                        ? null //
                        : passwordEncoder.encode(rawPassword);

        if (repository.existsByEmail(userEmail)) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        final User user = new User(userEmail, encodedPassword, //
                provider, providerSubject, //
                userRole);

        final User savedUser = repository.save(user);
        return this.toDTO(savedUser);
    }

    @Override
    public UserResponse getUserById(final Long id) {
        final User user = repository.findById(id) //
                .orElseThrow( //
                        () -> new ResourceNotFoundException("User not found with id: " + id));

        return this.toDTO(user);
    }

    @Override
    public Optional<UserResponse> findUser( //
            final String provider, final String providerSubject) {

        return repository //
                .findByProviderAndProviderSubject(provider, providerSubject) //
                .map(this::toDTO);
    }

    @Override
    public Optional<UserResponse> findUserByEmail(final String email) {
        return repository.findByEmail(email).map(this::toDTO);
    }


    // Helper Methods
    // ------------------------------------------------------

    private void validateProviderCredentials(final String provider, //
            final String providerSubject, final String password) {

        if (AuthProviderConstants.LOCAL_PROVIDER.equals(provider)) {
            if (Objects.isNull(password) || password.isBlank()) {
                throw new BadRequestException("Password is required for LOCAL users");
            }

            if (Objects.nonNull(providerSubject)) {
                throw new BadRequestException("Provider subject must be empty for LOCAL users");
            }

            return;
        }

        if (AuthProviderConstants.GOOGLE_PROVIDER.equals(provider)) {
            if (Objects.nonNull(password)) {
                throw new BadRequestException("Password must be empty for GOOGLE users");
            }

            if (Objects.isNull(providerSubject) || providerSubject.isBlank()) {
                throw new BadRequestException("Provider subject is required for GOOGLE users");
            }

            return;
        }

        throw new BadRequestException("Unsupported authentication provider");
    }

    private UserResponse toDTO(final User user) {

        return new UserResponse(user.getId(), user.getEmail(), //
                user.getRole(), user.getActive(), user.getCreatedAt(), //
                user.getUpdatedAt());
    }

}
