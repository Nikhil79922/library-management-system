package com.nikkssy.library_management.dto.auth.response;

public record RegistrationResponse(
        String email,
        boolean emailVerified,
        boolean phoneVerified
) {}