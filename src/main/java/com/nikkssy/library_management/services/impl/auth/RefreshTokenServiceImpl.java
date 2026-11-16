package com.nikkssy.library_management.services.impl.auth;

import com.nikkssy.library_management.dto.auth.response.RefreshTokenResponse;
import com.nikkssy.library_management.entity.RefreshToken;
import com.nikkssy.library_management.entity.Users;

public interface RefreshTokenServiceImpl {

    RefreshTokenResponse createRefreshToken(Users user);

    RefreshToken validateRefreshToken(String rawToken);

    void revokeToken(String rawToken);

    int revokeAllUserTokens(String userName);
}