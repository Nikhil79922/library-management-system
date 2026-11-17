package com.nikkssy.library_management.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPhoneRequest(

        @NotBlank(message = "Country is required")
        String country,

        @NotBlank(message = "Country calling code is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{0,3}$",
                message = "Invalid country calling code"
        )
        String countryCode,

        @NotBlank(message = "Country sort code is required")
        @Size(min = 2, max = 3)
        String countrySortCode,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{6,15}$",
                message = "Invalid phone number"
        )
        String phone
) {}