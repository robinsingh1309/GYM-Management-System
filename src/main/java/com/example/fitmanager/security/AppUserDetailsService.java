package com.example.fitmanager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.fitmanager.entity.User;
import com.example.fitmanager.repository.UserRepository;


@Service
public class AppUserDetailsService implements UserDetailsService {

    // Fields

    private final UserRepository repository;


    // Constructors
    // ------------------------------------------------------------

    @Autowired
    public AppUserDetailsService(final UserRepository repository) {
        this.repository = repository;
    }


    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {

        final User user = repository.findByEmail(userEmail) //
                .orElseThrow( //
                        () -> new UsernameNotFoundException("USER not found"));

        return new AppUserDetails(user);
    }

}
