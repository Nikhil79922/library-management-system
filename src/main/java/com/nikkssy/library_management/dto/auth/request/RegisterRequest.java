
package com.nikkssy.library_management.dto.auth.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "Username can contain only letters, numbers and underscores"
        )
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone,

        @Size(max = 10, message = "Country code must not exceed 10 characters")
        String countryCode,

        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @Size(max = 10, message = "Country short code must not exceed 10 characters")
        String countrySortCode

) {}
