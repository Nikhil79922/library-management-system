package com.nikkssy.library_management.dto.auth.response;

import java.time.Instant;

public record RefreshTokenResponse(
        String refreshToken,
        Instant expiryDate
) {}