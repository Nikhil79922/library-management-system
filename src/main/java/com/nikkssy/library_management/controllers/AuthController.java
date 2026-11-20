
package com.nikkssy.library_management.controllers;

import com.nikkssy.library_management.dto.auth.request.*;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.dto.auth.response.AuthResponse;
import com.nikkssy.library_management.dto.auth.response.RegistrationResponse;
import com.nikkssy.library_management.dto.commonRes.SuccessResponse;
import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.services.helper.cookies.AuthCookieManagement;
import com.nikkssy.library_management.services.impl.auth.AuthServiceImpl;
import com.nikkssy.library_management.services.impl.auth.PasswordResetServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
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

    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<RegistrationResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegistrationResponse result = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new SuccessResponse<>(
                                true,
                                "Registration initiated. Please verify your email and phone number.",
                                result,
                                LocalDateTime.now(),
                                HttpStatus.CREATED.value()
                        )
                );
    }

    @PostMapping("/otp/email/verify")
    public ResponseEntity<SuccessResponse<RegistrationResponse>> verifyEmailOtp(
            @Valid @RequestBody VerifyEmailOtpRequest request
    ) {

        RegistrationResponse result = authService.verifyEmailOtp(
                request.email(),
                request.otp()
        );

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Email verified successfully.",
                        result,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }


    @PostMapping("/otp/phone/send")
    public ResponseEntity<SuccessResponse<RegistrationResponse>> sendPhoneOtp(
            @Valid @RequestBody SendPhoneOtpRequest request
    ) {

        RegistrationResponse result =
                authService.sendPhoneOtp(request.email());

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Phone verification OTP generated successfully.",
                        result,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }


    @PostMapping("/otp/phone/verify")
    public ResponseEntity<SuccessResponse<AuthResponse>> verifyPhoneOtp(
            @Valid @RequestBody VerifyPhoneOtpRequest request
    ) {

        AuthResult result = authService.verifyPhoneOtp(
                request.email(),
                request.otp()
        );

        ResponseCookie cookie =
                cookieManagement.createRefreshTokenCookie(
                        result.refreshToken().refreshToken(),
                        result.refreshToken().expiryDate()
                );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "Registration completed successfully.",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }


    @PostMapping("/phone/register")
    public ResponseEntity<SuccessResponse<RegistrationResponse>> registerPhone(
            @Valid @RequestBody RegisterPhoneRequest request,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {

        if (oidcUser == null) {
            throw new OAuth2AuthenticationException(
                    "Google authentication required"
            );
        }

        RegistrationResponse result = authService.registerPhone(
                oidcUser.getSubject(),
                request
        );

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "Phone details saved. Please verify your OTP.",
                        result,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
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

    // LOGIN WITH EMAIL OTP: SEND
    @PostMapping("/otp/login/email/send")
    public ResponseEntity<SuccessResponse<Void>> sendEmailLoginOtp(
            @Valid @RequestBody EmailOtpLoginRequest request
    ) {

        authService.sendEmailLoginOtp(request);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "If an eligible account exists, an OTP has been sent.",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // LOGIN WITH PHONE OTP: SEND
    @PostMapping("/otp/login/phone/send")
    public ResponseEntity<SuccessResponse<Void>> sendPhoneLoginOtp(
            @Valid @RequestBody PhoneOtpLoginRequest request
    ) {

        authService.sendPhoneLoginOtp(request);

        return ResponseEntity.ok(
                new SuccessResponse<>(
                        true,
                        "If an eligible account exists, an OTP has been sent.",
                        null,
                        LocalDateTime.now(),
                        HttpStatus.OK.value()
                )
        );
    }

    // LOGIN WITH EMAIL OTP: VERIFY
    @PostMapping("/otp/login/email/verify")
    public ResponseEntity<SuccessResponse<AuthResponse>> verifyEmailLoginOtp(
            @Valid @RequestBody VerifyEmailOtpLoginRequest request
    ) {

        AuthResult result = authService.verifyEmailLoginOtp(request);

        ResponseCookie cookie = cookieManagement.createRefreshTokenCookie(
                result.refreshToken().refreshToken(),
                result.refreshToken().expiryDate()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User logged in successfully.",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // LOGIN WITH PHONE OTP: VERIFY
    @PostMapping("/otp/login/phone/verify")
    public ResponseEntity<SuccessResponse<AuthResponse>> verifyPhoneLoginOtp(
            @Valid @RequestBody VerifyPhoneOtpLoginRequest request
    ) {

        AuthResult result = authService.verifyPhoneLoginOtp(request);

        ResponseCookie cookie = cookieManagement.createRefreshTokenCookie(
                result.refreshToken().refreshToken(),
                result.refreshToken().expiryDate()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "User logged in successfully.",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

}
