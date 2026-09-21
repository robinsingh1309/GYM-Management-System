package com.example.fitmanager.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class CookieOAuth2AuthorizationRequestRepository //
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    // Fields

    public static final String COOKIE_NAME = "FITMANAGER_OAUTH2_REQUEST";

    private static final int COOKIE_LIFETIME_SECONDS = 300;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Boolean secureCookie;


    // Constructors
    // -------------------------------------------------------

    public CookieOAuth2AuthorizationRequestRepository( //
            final @Value("${oauth2.cookie-secret}") String encodedSecret, //
            final @Value("${oauth2.cookie-secure:false}") Boolean secureCookie) {

        final byte[] key = Base64.getDecoder().decode(encodedSecret);

        if (key.length != 32) {
            throw new IllegalArgumentException("OAuth2 cookie secret must be a Base64-encoded 256-bit key");
        }

        this.secretKey = new SecretKeySpec(key, "AES");
        this.secureCookie = secureCookie;
    }


    // AuthorizationRequestRepository Methods
    // -------------------------------------------------------

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(final HttpServletRequest request) {
        return readCookie(request);
    }

    @Override
    public void saveAuthorizationRequest(final OAuth2AuthorizationRequest authorizationRequest, //
            final HttpServletRequest request, final HttpServletResponse response) {

        if (Objects.isNull(authorizationRequest)) {
            deleteCookie(response);
            return;
        }

        final Cookie cookie = new Cookie(COOKIE_NAME, encrypt(authorizationRequest));
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(COOKIE_LIFETIME_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest( //
            final HttpServletRequest request, final HttpServletResponse response) {

        final OAuth2AuthorizationRequest authorizationRequest = readCookie(request);
        deleteCookie(response);
        return authorizationRequest;
    }


    // Helper Methods
    // -------------------------------------------------------

    private OAuth2AuthorizationRequest readCookie(final HttpServletRequest request) {

        final Cookie[] cookies = request.getCookies();

        if (Objects.isNull(cookies)) {
            return null;
        }

        for (final Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return decrypt(cookie.getValue());
            }
        }

        return null;
    }

    private String encrypt(final OAuth2AuthorizationRequest authorizationRequest) {

        try {
            final ByteArrayOutputStream serialized = new ByteArrayOutputStream();

            try (ObjectOutputStream output = new ObjectOutputStream(serialized)) {
                output.writeObject(authorizationRequest);
            }

            final byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            final byte[] encrypted = cipher.doFinal(serialized.toByteArray());
            final byte[] value = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, value, 0, iv.length);
            System.arraycopy(encrypted, 0, value, iv.length, encrypted.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to protect OAuth2 authorization request", exception);
        }
    }

    private OAuth2AuthorizationRequest decrypt(final String value) {

        try {
            final byte[] encrypted = Base64.getUrlDecoder().decode(value);

            if (encrypted.length <= IV_LENGTH_BYTES) {
                return null;
            }

            final byte[] iv = new byte[IV_LENGTH_BYTES];
            final byte[] ciphertext = new byte[encrypted.length - IV_LENGTH_BYTES];
            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            System.arraycopy(encrypted, iv.length, ciphertext, 0, ciphertext.length);

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            try (final ObjectInputStream input = new ObjectInputStream( //
                    new ByteArrayInputStream(cipher.doFinal(ciphertext)))) {

                return (OAuth2AuthorizationRequest) input.readObject();
            }
        } catch (final Exception exception) {
            return null;
        }
    }

    private void deleteCookie(final HttpServletResponse response) {
        final Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }
}
