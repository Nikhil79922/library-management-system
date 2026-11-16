
package com.nikkssy.library_management.controllers;

import com.nikkssy.library_management.dto.auth.request.LoginRequest;
import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.dto.auth.request.ForgotPasswordRequest;
import com.nikkssy.library_management.dto.auth.request.ResetPasswordRequest;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.dto.auth.response.AuthResponse;
import com.nikkssy.library_management.dto.commonRes.SuccessResponse;
import com.nikkssy.library_management.services.helper.cookies.AuthCookieManagement;
import com.nikkssy.library_management.services.impl.auth.AuthServiceImpl;
import com.nikkssy.library_management.services.impl.auth.PasswordResetServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AuthController {

    private final AuthServiceImpl authService;
    private final PasswordResetServiceImpl passwordResetService;
    private final AuthCookieManagement cookieManagement;

    // Register new user
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResult result = authService.register(request);

        ResponseCookie cookie = cookieManagement.createRefreshTokenCookie(
                result.refreshToken().refreshToken(),
                result.refreshToken().expiryDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User registered successfully",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.CREATED.value()
                        )
                );
    }

    // Authenticate user
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResult result = authService.login(request);

        ResponseCookie cookie = cookieManagement.createRefreshTokenCookie(
                result.refreshToken().refreshToken(),
                result.refreshToken().expiryDate()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User logged in successfully",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // Generate new access and refresh tokens
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<AuthResponse>> refreshToken(
            @CookieValue("refreshToken") String refreshToken
    ) {

        AuthResult result = authService.refreshToken(refreshToken);

        ResponseCookie cookie = cookieManagement.createRefreshTokenCookie(
                result.refreshToken().refreshToken(),
                result.refreshToken().expiryDate()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "Tokens refreshed successfully",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // Logout from current session
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @CookieValue(
                    value = "refreshToken",
                    required = false
            ) String refreshToken
    ) {

        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logOut(refreshToken);
        }

        ResponseCookie cookie = cookieManagement.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User logged out successfully",
                                null,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // Logout from all active sessions
    @PostMapping("/logout-all")
    public ResponseEntity<SuccessResponse<Integer>> logoutAll(
            Authentication authentication
    ) {

        String username = authentication.getName();

        int revokedCount = authService.logOutAll(username);

        ResponseCookie cookie = cookieManagement.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User logged out from all devices successfully",
                                revokedCount,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // Request password reset email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request
    ) {
        // Business logic
        passwordResetService.forgotPassword(request.email());

        return ResponseEntity.ok()
                .body(
                        new SuccessResponse<>(
                                true,
                                "If an account exists with this email, a password reset link has been sent",
                                null,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // Reset user password
    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return ResponseEntity.ok()
                .body(
                        new SuccessResponse<>(
                                true,
                                "Password has been reset successfully",
                                null,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }
}
