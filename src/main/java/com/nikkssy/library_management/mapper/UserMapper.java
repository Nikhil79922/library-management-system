
package com.nikkssy.library_management.mapper;

import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.dto.auth.response.UserResponse;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public Users toEntity(RegisterRequest request) {

        Users user = new Users();

        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setCountryCode(request.countryCode());
        user.setCountry(request.country());
        user.setCountrySortCode(request.countrySortCode());

        user.setProvider(AuthProvider.LOCAL);
        user.setEnabled(true);
        user.setRole(Role.USER);

        return user;
    }

    public UserResponse toResponse(Users user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCountryCode(),
                user.getCountry(),
                user.getCountrySortCode(),
                user.getProvider(),
                user.getEnabled(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
