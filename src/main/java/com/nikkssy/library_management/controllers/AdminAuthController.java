
package com.nikkssy.library_management.controllers;

import com.nikkssy.library_management.dto.auth.request.LoginRequest;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.dto.auth.response.AuthResponse;
import com.nikkssy.library_management.dto.commonRes.SuccessResponse;
import com.nikkssy.library_management.services.helper.cookies.AuthCookieManagement;

import com.nikkssy.library_management.services.impl.auth.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthServiceImpl authService;
    private final AuthCookieManagement cookieManagement;

    // ADMIN LOGIN
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResult result = authService.adminLogin(request);

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
                                "Admin logged in successfully.",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // REFRESH ADMIN TOKENS
    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse<AuthResponse>> refreshToken(
            @CookieValue("refreshToken") String refreshToken
    ) {

        AuthResult result = authService.refreshToken(refreshToken);

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
                                "Admin tokens refreshed successfully.",
                                result.authResponse(),
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // ADMIN LOGOUT - CURRENT SESSION
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

        ResponseCookie cookie =
                cookieManagement.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "Admin logged out successfully.",
                                null,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }

    // ADMIN LOGOUT - ALL DEVICES
    @PostMapping("/logout-all")
    public ResponseEntity<SuccessResponse<Integer>> logoutAll(
            Authentication authentication
    ) {

        String username = authentication.getName();

        int revokedCount = authService.logOutAll(username);

        ResponseCookie cookie =
                cookieManagement.clearRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        new SuccessResponse<>(
                                true,
                                "Admin logged out from all devices successfully.",
                                revokedCount,
                                LocalDateTime.now(),
                                HttpStatus.OK.value()
                        )
                );
    }
}
