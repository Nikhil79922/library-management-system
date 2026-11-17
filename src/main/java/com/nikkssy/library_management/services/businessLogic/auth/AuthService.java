package com.nikkssy.library_management.services.businessLogic.auth;

import com.nikkssy.library_management.dto.auth.request.LoginRequest;
import com.nikkssy.library_management.dto.auth.request.RegisterPhoneRequest;
import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.dto.auth.response.RegistrationResponse;
import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.entity.enums.OtpPurpose;
import com.nikkssy.library_management.exception.AccessDeniedException;
import com.nikkssy.library_management.exception.IllegalArgumentException;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.entity.RefreshToken;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.mapper.auth.AuthResultMapper;
import com.nikkssy.library_management.mapper.UserMapper;
import com.nikkssy.library_management.model.CustomUserDetails;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.repository.UserRepository;
import com.nikkssy.library_management.services.helper.otp.OtpService;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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
    private final OtpService otpService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    // SIGNUP
    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest registerRequest) {

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

        // 1. Save user
        Users savedUser = userRepository.save(user);

        // 2. Create LOCAL authentication mapping
        UserAuthProvider authProvider =
                userMapper.toLocalAuthProvider(savedUser);

        savedUser.addAuthProvider(authProvider);
        userAuthProviderRepository.save(authProvider);

        // 3. Generate and send registration email OTP
        otpService.generateAndSendRegistrationEmailOtp(savedUser);

        // 4. Return pending registration response
        return authResultMapper.toRegistrationResponse(
                savedUser.getEmail(),
                false,
                false
        );
    }


    @Override
    @Transactional
    public RegistrationResponse verifyEmailOtp(
            String email,
            String otp
    ) {

        // 1. Find user
        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        // 2. Check whether email is already verified
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Email is already verified"
            );
        }

        // 3. Verify registration email OTP
        otpService.verifyOtp(
                OtpPurpose.REGISTRATION_EMAIL,
                user.getEmail(),
                otp
        );

        // 4. Update verification status
        user.setEmailVerified(true);

        Users savedUser = userRepository.save(user);

        // 5. Return updated registration status
        return authResultMapper.toRegistrationResponse(
                savedUser.getEmail(),
                savedUser.isEmailVerified(),
                savedUser.isPhoneVerified()
        );
    }

    @Override
    @Transactional
    public RegistrationResponse sendPhoneOtp(String email) {

        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Please verify your email first"
            );
        }

        if (user.isPhoneVerified()) {
            throw new IllegalArgumentException(
                    "Phone number is already verified"
            );
        }

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new IllegalArgumentException(
                    "Phone number is not registered"
            );
        }

        otpService.generateAndLogRegistrationPhoneOtp(user);

        return authResultMapper.toRegistrationResponse(
                user.getEmail(),
                user.isEmailVerified(),
                user.isPhoneVerified()
        );
    }


    @Override
    @Transactional
    public AuthResult verifyPhoneOtp(
            String email,
            String otp
    ) {

        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Please verify your email first"
            );
        }

        if (user.isPhoneVerified()) {
            throw new IllegalArgumentException(
                    "Phone number is already verified"
            );
        }

        String identifier =
                user.getCountryCode() + ":" + user.getPhone();

        // 1. Verify phone OTP
        otpService.verifyOtp(
                OtpPurpose.REGISTRATION_PHONE,
                identifier,
                otp
        );

        // 2. Mark phone as verified
        user.setPhoneVerified(true);

        Users savedUser = userRepository.save(user);

        // 3. Generate access token
        String accessToken = jwtService.generateToken(savedUser);

        // 4. Generate refresh token
        var refreshToken =
                refreshTokenService.createRefreshToken(savedUser);

        // 5. Send welcome email
        sendWelcomeEmail(savedUser);

        // 6. Return authentication result
        return authResultMapper.toAuthResult(
                accessToken,
                refreshToken,
                savedUser
        );
    }


    @Override
    @Transactional
    public RegistrationResponse registerPhone(
            String googleSub,
            RegisterPhoneRequest request
    ) {

        UserAuthProvider provider = userAuthProviderRepository
                .findByProviderAndProviderId(
                        AuthProvider.GOOGLE,
                        googleSub
                )
                .orElseThrow(() ->
                        new OAuth2AuthenticationException(
                                "Google account not found"
                        )
                );

        Users user = provider.getUser();

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalArgumentException("Your account is disabled");
        }

        if (user.isPhoneVerified()) {
            throw new IllegalArgumentException(
                    "Phone number is already verified"
            );
        }

        // Prevent a phone number from being assigned to another user.
        boolean phoneExists = userRepository.existsByCountryCodeAndPhone(
                request.countryCode(),
                request.phone()
        );

        if (phoneExists) {
            throw new IllegalArgumentException(
                    "Phone number is already registered"
            );
        }

        user.setCountry(request.country());
        user.setCountryCode(request.countryCode());
        user.setCountrySortCode(request.countrySortCode());
        user.setPhone(request.phone());

        Users savedUser = userRepository.save(user);

        otpService.generateAndLogRegistrationPhoneOtp(savedUser);

        return authResultMapper.toRegistrationResponse(
                savedUser.getEmail(),
                savedUser.isEmailVerified(),
                savedUser.isPhoneVerified()
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

        // Ensure registration is completed.
        if (!user.isEmailVerified() ||
                !user.isPhoneVerified()) {

            throw new AccessDeniedException(
                    "Please complete your email and phone verification before logging in."
            );
        }

        // Generate access token.
        String accessToken = jwtService.generateToken(authentication);

        // Generate and persist refresh token.
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