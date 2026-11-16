package com.nikkssy.library_management.mapper.auth;

import com.nikkssy.library_management.dto.auth.request.ForgotPasswordRequest;
import com.nikkssy.library_management.dto.auth.request.ResetPasswordRequest;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class PasswordResetMapper {

    public String toEmail(ForgotPasswordRequest request) {
        return normalizeEmail(request.email());
    }

    public String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String toResetToken(ResetPasswordRequest request) {
        return request.token().trim();
    }

    public String toNewPassword(ResetPasswordRequest request) {
        return request.newPassword();
    }
}
