package com.nikkssy.library_management.services.impl.auth;

import com.nikkssy.library_management.dto.auth.request.*;
import com.nikkssy.library_management.dto.auth.response.AuthResult;
import com.nikkssy.library_management.dto.auth.response.RegistrationResponse;

public interface AuthServiceImpl {
    RegistrationResponse register(RegisterRequest registerRequest);

    RegistrationResponse verifyEmailOtp(String email, String otp);

    RegistrationResponse sendPhoneOtp(String email);

    AuthResult verifyPhoneOtp(String email, String otp);

    RegistrationResponse registerPhone(
            String googleSub,
            RegisterPhoneRequest request
    );
    AuthResult login(LoginRequest loginRequest);

    AuthResult refreshToken(String rawToken);

    void logOut(String rawToken);

    int logOutAll(String userName);

    void sendEmailLoginOtp(EmailOtpLoginRequest request);

    void sendPhoneLoginOtp(PhoneOtpLoginRequest request);

    AuthResult verifyEmailLoginOtp(VerifyEmailOtpLoginRequest request);

    AuthResult verifyPhoneLoginOtp(VerifyPhoneOtpLoginRequest request);

    AuthResult adminLogin(LoginRequest request);
}
