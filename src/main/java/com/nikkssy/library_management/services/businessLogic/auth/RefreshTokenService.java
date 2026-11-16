package com.nikkssy.library_management.services.businessLogic.auth;

import com.nikkssy.library_management.dto.auth.response.RefreshTokenResponse;
import com.nikkssy.library_management.exception.IllegalArgumentException;
import com.nikkssy.library_management.exception.IllegalStateException;
import com.nikkssy.library_management.entity.RefreshToken;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.mapper.auth.RefreshTokenMapper;
import com.nikkssy.library_management.repository.RefreshTokenRepository;
import com.nikkssy.library_management.services.impl.auth.RefreshTokenServiceImpl;
import com.nikkssy.library_management.services.impl.auth.TokenHelperServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenServiceImpl{
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;
    private final TokenHelperServiceImpl tokenHelperService;

    @Value("${jwt.refresh-token.expiry}")
    private long refreshTokenExpiry;

    @Override
    @Transactional
    public RefreshTokenResponse createRefreshToken(Users user) {

        String rawToken = tokenHelperService.generateSecureToken();
        String tokenHash = tokenHelperService.hashToken(rawToken);

        Instant expiryDate = Instant.now()
                .plusSeconds(refreshTokenExpiry);

        RefreshToken refreshToken = refreshTokenMapper.toEntity(
                tokenHash,
                user,
                expiryDate
        );

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        return refreshTokenMapper.toResponse(rawToken, savedToken);
    }

    @Override
    @Transactional
    public RefreshToken validateRefreshToken(String rawToken) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashForUpdate(
                        tokenHelperService.hashToken(rawToken)
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token")
                );

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException(
                    "Refresh token has been revoked"
            );
        }

        if (!refreshToken.getExpiryDate().isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Refresh token has expired"
            );
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(tokenHelperService.hashToken(rawToken))
                .orElseThrow(() ->
                        new IllegalStateException("Invalid refresh token")
                );

        token.setRevoked(true);
    }

    @Override
    @Transactional
    public int revokeAllUserTokens(String userName) {

        int revokedCount = refreshTokenRepository.revokeAllByUserName(userName);

        if (revokedCount == 0) {
            throw new IllegalStateException(
                    "No active refresh tokens found for user ID: " + userName
            );
        }
        return revokedCount;
    }
}
