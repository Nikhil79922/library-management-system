package com.nikkssy.library_management.services.businessLogic.auth;

import com.nikkssy.library_management.dto.auth.request.LoginRequest;
import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.exception.IllegalArgumentException;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.entity.RefreshToken;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.mapper.auth.AuthResultMapper;
import com.nikkssy.library_management.mapper.UserMapper;
import com.nikkssy.library_management.model.CustomUserDetails;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.repository.UserRepository;
import com.nikkssy.library_management.services.helper.security.jwt.JwtService;
import com.nikkssy.library_management.services.impl.auth.AuthServiceImpl;
import com.nikkssy.library_management.services.impl.auth.RefreshTokenServiceImpl;
import com.nikkssy.library_management.services.impl.email.EmailServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceImpl {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthResultMapper authResultMapper;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailServiceImpl emailService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // SIGNUP
    @Override
    @Transactional
    public AuthResult register(RegisterRequest registerRequest) {

        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Users user = userMapper.toEntity(registerRequest);

        user.setPassword(
                passwordEncoder.encode(registerRequest.password())
        );

        // 1. Save user first to generate the ID
        Users savedUser = userRepository.save(user);

        // 2. Create LOCAL authentication mapping
        UserAuthProvider authProvider =
                userMapper.toLocalAuthProvider(savedUser);

        // 3. Associate provider with user
        savedUser.addAuthProvider(authProvider);

        userAuthProviderRepository.save(authProvider);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                registerRequest.username(),
                                registerRequest.password()
                        )
                );

        String accessToken = jwtService.generateToken(authentication);

        var refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        // 8. Send welcome email
        sendWelcomeEmail(savedUser);

        return authResultMapper.toAuthResult(
                accessToken,
                refreshToken,
                savedUser
        );
    }


    // LOGIN
    @Override
    @Transactional
    public AuthResult login(LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.usernameOrEmail(),
                                loginRequest.password()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Users user = userDetails.getUser();

        String accessToken = jwtService.generateToken(authentication);

        var refreshToken =
                refreshTokenService.createRefreshToken(user);

        return authResultMapper.toAuthResult(
                accessToken,
                refreshToken,
                user
        );
    }

    // REFRESH TOKEN
    @Override
    @Transactional
    public AuthResult refreshToken(String rawToken) {

        RefreshToken existingToken =
                refreshTokenService.validateRefreshToken(rawToken);

        Users user = existingToken.getUser();

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalStateException("User account is disabled");
        }

        refreshTokenService.revokeToken(rawToken);

        String accessToken = jwtService.generateToken(user);

        var newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return authResultMapper.toAuthResult(
                accessToken,
                newRefreshToken,
                user
        );
    }

    // LOGOUT
    @Override
    @Transactional
    public void logOut(String rawToken) {
        refreshTokenService.revokeToken(rawToken);
    }

    // LOGOUT ALL
    @Override
    @Transactional
    public int logOutAll(String userName) {
        return refreshTokenService.revokeAllUserTokens(userName);
    }



    private void sendWelcomeEmail(Users user) {

        // A mail failure must not fail a successful registration.
        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getUsername(),
                    frontendBaseUrl
            );
        } catch (Exception e) {
            log.warn("Welcome email failed for user {}", user.getUsername(), e);
        }
    }
}