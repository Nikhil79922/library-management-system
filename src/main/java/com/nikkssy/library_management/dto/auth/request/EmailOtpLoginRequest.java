package com.nikkssy.library_management.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailOtpLoginRequest(
        @NotBlank
        @Email
        String email
) {}