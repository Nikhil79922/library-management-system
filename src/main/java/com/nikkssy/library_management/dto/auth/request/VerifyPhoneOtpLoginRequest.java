package com.nikkssy.library_management.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPhoneOtpLoginRequest(

        @NotBlank
        String countryCode,

        @NotBlank
        String phoneNumber,

        @NotBlank
        @Pattern(
                regexp = "\\d{6}",
                message = "OTP must contain exactly 6 digits"
        )
        String otp

) {}