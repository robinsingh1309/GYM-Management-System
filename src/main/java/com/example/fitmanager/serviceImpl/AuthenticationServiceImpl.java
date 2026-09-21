package com.example.fitmanager.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.fitmanager.constant.AuthProviderConstants;
import com.example.fitmanager.dto.AuthenticationRequest;
import com.example.fitmanager.dto.AuthenticationResponse;
import com.example.fitmanager.dto.TokenResponse;
import com.example.fitmanager.dto.UserCreateRequest;
import com.example.fitmanager.dto.UserResponse;
import com.example.fitmanager.entity.Role;
import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.security.JwtService;
import com.example.fitmanager.service.AuthenticationService;
import com.example.fitmanager.service.OAuth2HandoffService;
import com.example.fitmanager.service.UserService;


@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    // Fields

    private final UserService userService;
    private final OAuth2HandoffService handoffService;
    private final JwtService jwtService;


    // Constructors
    // -------------------------------------------------------------------

    @Autowired
    public AuthenticationServiceImpl(final UserService userService, //
            final OAuth2HandoffService handoffService, final JwtService jwtService) {

        this.userService = userService;
        this.handoffService = handoffService;
        this.jwtService = jwtService;
    }


    // Methods
    // -------------------------------------------------------------------

    @Override
    public AuthenticationResponse register(final AuthenticationRequest request) {

        final String userEmail = request.getEmail();
        final String userPassword = request.getPassword();

        final UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setEmail(userEmail);
        userCreateRequest.setPassword(userPassword);
        userCreateRequest.setProvider(AuthProviderConstants.LOCAL_PROVIDER);
        userCreateRequest.setProviderSubject(null);
        userCreateRequest.setRole(Role.STAFF);

        final UserResponse userResponse = userService.createUser(userCreateRequest);

        return new AuthenticationResponse(userResponse.getId(), //
                userResponse.getUserEmail(), userResponse.getRole());
    }

    @Override
    public UserResponse authenticateGoogleUser(final String providerSubject, final String email) {

        final UserResponse existingGoogleUser = userService //
                .findUser(AuthProviderConstants.GOOGLE_PROVIDER, providerSubject) //
                .orElse(null);

        if (existingGoogleUser != null) {
            if (!Boolean.TRUE.equals(existingGoogleUser.getActive())) {
                throw new BadRequestException("ACCOUNT_INACTIVE");
            }

            return existingGoogleUser;
        }

        if (userService.findUserByEmail(email).isPresent()) {
            throw new BadRequestException("EMAIL_ALREADY_REGISTERED");
        }

        final UserCreateRequest userCreateRequest = new UserCreateRequest();
        userCreateRequest.setEmail(email);
        userCreateRequest.setPassword(null);
        userCreateRequest.setProvider(AuthProviderConstants.GOOGLE_PROVIDER);
        userCreateRequest.setProviderSubject(providerSubject);
        userCreateRequest.setRole(Role.STAFF);

        return userService.createUser(userCreateRequest);
    }

    @Override
    public TokenResponse exchangeOAuth2Code(final String code) {

        final Long userId = handoffService.consumeCode(code);
        final UserResponse user = userService.getUserById(userId);

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BadRequestException("ACCOUNT_INACTIVE");
        }

        final SimpleGrantedAuthority authority = //
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        final UsernamePasswordAuthenticationToken authentication = //
                UsernamePasswordAuthenticationToken.authenticated( //
                        user.getUserEmail(), null, List.of(authority));

        return jwtService.generateToken(authentication);
    }

}
