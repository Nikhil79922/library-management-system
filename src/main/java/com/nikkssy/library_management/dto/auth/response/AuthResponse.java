
package com.nikkssy.library_management.dto.auth.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {}
