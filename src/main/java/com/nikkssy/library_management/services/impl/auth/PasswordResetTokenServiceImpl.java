package com.nikkssy.library_management.services.impl.auth;

import com.nikkssy.library_management.entity.Users;

public interface PasswordResetTokenServiceImpl {

    String generateResetToken(Users user);

    Users validateResetToken(String token);
}
