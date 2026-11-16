package com.nikkssy.library_management.services.impl.auth;

public interface PasswordResetServiceImpl {
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
