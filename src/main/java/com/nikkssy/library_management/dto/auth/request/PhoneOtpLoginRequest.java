package com.nikkssy.library_management.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record PhoneOtpLoginRequest(
        @NotBlank
        String countryCode,

        @NotBlank
        String phoneNumber
) {}
