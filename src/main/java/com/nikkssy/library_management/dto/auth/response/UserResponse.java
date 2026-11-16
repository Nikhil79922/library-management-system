
package com.nikkssy.library_management.dto.auth.response;

import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.entity.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String fullName,
        String username,
        String email,
        String phone,
        String countryCode,
        String country,
        String countrySortCode,
        AuthProvider provider,
        Boolean enabled,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}
