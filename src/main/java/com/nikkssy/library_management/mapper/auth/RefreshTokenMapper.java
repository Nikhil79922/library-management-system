
package com.nikkssy.library_management.mapper.auth;

import com.nikkssy.library_management.dto.auth.response.RefreshTokenResponse;
import com.nikkssy.library_management.entity.RefreshToken;
import com.nikkssy.library_management.entity.Users;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenMapper {

    public RefreshToken toEntity(
            String tokenHash,
            Users user,
            Instant expiryDate
    ) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setTokenHash(tokenHash);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);

        return refreshToken;
    }

    public RefreshTokenResponse toResponse(
            String rawToken,
            RefreshToken refreshToken
    ) {
        return new RefreshTokenResponse(
                rawToken,
                refreshToken.getExpiryDate()
        );
    }
}
