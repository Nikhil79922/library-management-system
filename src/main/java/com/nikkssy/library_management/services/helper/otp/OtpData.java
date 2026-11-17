package com.nikkssy.library_management.services.helper.otp;

import java.time.Instant;

public record OtpData(
        String hashedOtp,
        int attempts,
        Instant expiresAt
) {}