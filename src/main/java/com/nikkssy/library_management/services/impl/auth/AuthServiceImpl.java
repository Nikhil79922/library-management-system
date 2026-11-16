package com.nikkssy.library_management.services.impl.auth;

import com.nikkssy.library_management.dto.auth.request.LoginRequest;
import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.dto.auth.response.AuthResult;

public interface AuthServiceImpl {
    AuthResult register(RegisterRequest registerRequest);

    AuthResult login(LoginRequest loginRequest);

    AuthResult refreshToken(String rawToken);

    void logOut(String rawToken);

    int logOutAll(String userName);
}
