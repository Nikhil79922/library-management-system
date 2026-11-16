package com.nikkssy.library_management.services.businessLogic.auth;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.mapper.auth.PasswordResetMapper;
import com.nikkssy.library_management.repository.UserRepository;
import com.nikkssy.library_management.services.impl.auth.PasswordResetServiceImpl;
import com.nikkssy.library_management.services.impl.auth.PasswordResetTokenServiceImpl;
import com.nikkssy.library_management.services.impl.email.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService implements PasswordResetServiceImpl {

    private final UserRepository userRepository;
    private final PasswordResetMapper passwordResetMapper;
    private final PasswordResetTokenServiceImpl passwordResetTokenService;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.password-reset-path}")
    private String passwordResetPath;

    @Override
    public void forgotPassword(String email) {

        String normalizedEmail =
                passwordResetMapper.normalizeEmail(email);

        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .filter(user -> Boolean.TRUE.equals(user.getEnabled()))
                .filter(user -> user.getPassword() != null)
                .ifPresent(this::sendPasswordResetEmail);
    }

    private void sendPasswordResetEmail(Users user) {

        String resetToken =
                passwordResetTokenService.generateResetToken(user);

        String encodedToken = URLEncoder.encode(
                resetToken,
                StandardCharsets.UTF_8
        );

        String resetLink = frontendBaseUrl
                + passwordResetPath
                + "?token="
                + encodedToken;

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetLink
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {

        String normalizedToken = token.trim();

        Users user =
                passwordResetTokenService.validateResetToken(normalizedToken);

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);

        // Revoke all active refresh tokens for this user
        refreshTokenService.revokeAllUserTokens(user.getUsername());

        sendPasswordChangedEmail(user);
    }

    private void sendPasswordChangedEmail(Users user) {

        // A mail failure must not roll back or fail a successful reset.
        try {
            emailService.sendNotificationEmail(
                    user.getEmail(),
                    "Your Library Management password was changed",
                    user.getUsername(),
                    """
                    Your password was changed successfully, and you have been signed out of all devices.

                    If you did not make this change, reset your password immediately and contact support.""",
                    "Log In",
                    frontendBaseUrl
            );
        } catch (Exception e) {
            log.warn("Password-changed email failed for user {}", user.getUsername(), e);
        }
    }
}