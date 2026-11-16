
package com.nikkssy.library_management.dto.auth.response;

public record AuthResult(
        AuthResponse authResponse,
        RefreshTokenResponse refreshToken
) {}
