package com.nikkssy.library_management.mapper.auth;

import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.dto.auth.response.AuthResponse;
import com.nikkssy.library_management.dto.auth.response.RefreshTokenResponse;
import com.nikkssy.library_management.entity.Users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthResultMapper {

    private final AuthMapper authMapper;

    public AuthResult toAuthResult(
            String accessToken,
            RefreshTokenResponse refreshToken,
            Users user
    ) {

        AuthResponse authResponse =
                authMapper.toAuthResponse(accessToken, user);

        return new AuthResult(
                authResponse,
                refreshToken
        );
    }
}