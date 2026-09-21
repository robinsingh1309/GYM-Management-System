package com.example.fitmanager.serviceImpl;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.fitmanager.exception.BadRequestException;
import com.example.fitmanager.service.OAuth2HandoffService;


@Service
public class OAuth2HandoffServiceImpl implements OAuth2HandoffService {

    // Fields

    private static final long CODE_LIFETIME_SECONDS = 60;

    private final SecureRandom secureRandom;
    private final Clock clock;

    // ponytail: in-memory codes support one backend instance;
    private final ConcurrentHashMap<String, Handoff> handoffs = new ConcurrentHashMap<>();


    // Constructors
    // -------------------------------------------------------

    public OAuth2HandoffServiceImpl() {
        this(new SecureRandom(), Clock.systemUTC());
    }

    OAuth2HandoffServiceImpl(final SecureRandom secureRandom, final Clock clock) {
        this.secureRandom = secureRandom;
        this.clock = clock;
    }


    // OAuth2HandoffService Methods
    // -------------------------------------------------------

    @Override
    public String createCode(final Long userId) {

        final Instant now = clock.instant();
        handoffs.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));

        final byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        final String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        handoffs.put(code, new Handoff(userId, now.plusSeconds(CODE_LIFETIME_SECONDS)));
        return code;
    }

    @Override
    public Long consumeCode(final String code) {

        if (Objects.isNull(code) || code.isBlank()) {
            throw new BadRequestException("INVALID_OAUTH2_CODE");
        }

        final Handoff handoff = handoffs.remove(code);

        if (Objects.isNull(handoff) || !handoff.expiresAt().isAfter(clock.instant())) {
            throw new BadRequestException("INVALID_OAUTH2_CODE");
        }

        return handoff.userId();
    }


    // Records
    // -------------------------------------------------------

    private record Handoff(Long userId, Instant expiresAt) {
    }
}
