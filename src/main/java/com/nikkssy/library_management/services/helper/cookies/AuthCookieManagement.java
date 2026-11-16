package com.nikkssy.library_management.services.helper.cookies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthCookieManagement {

    @Value("${app.security.refresh-cookie-secure}")
    private boolean secureCookie;

    @Value("${app.security.refresh-cookie-same-site:Lax}")
    private String sameSite;

    // Create refresh token cookie
    public ResponseCookie createRefreshTokenCookie(
            String refreshToken,
            Instant expiryDate
    ) {

        Duration maxAge = Duration.between(
                Instant.now(),
                expiryDate
        );

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/auth")
                .sameSite(sameSite)
                .maxAge(maxAge.isNegative() ? Duration.ZERO : maxAge)
                .build();
    }

    // Clear refresh token cookie
    public ResponseCookie clearRefreshTokenCookie() {

        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/auth")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();
    }
}
