
package com.nikkssy.library_management.mapper;

import com.nikkssy.library_management.dto.auth.request.RegisterRequest;
import com.nikkssy.library_management.dto.auth.response.UserResponse;
import com.nikkssy.library_management.entity.UserAuthProvider;
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
        user.setPhoneVerified(false);
        user.setEmailVerified(false);

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
                user.getEnabled(),
                user.isEmailVerified(),
                user.isPhoneVerified(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public Users toGoogleUser(
            String fullName,
            String email,
            String username
    ) {
        Users user = new Users();

        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(null);
        user.setEnabled(true);
        user.setRole(Role.USER);
        user.setEmailVerified(true);
        user.setPhoneVerified(false);

        return user;
    }

    public UserAuthProvider toGoogleAuthProvider(
            Users user,
            String googleSub
    ) {
        UserAuthProvider authProvider = new UserAuthProvider();

        authProvider.setProvider(AuthProvider.GOOGLE);
        authProvider.setProviderId(googleSub);

        user.addAuthProvider(authProvider);

        return authProvider;
    }

    public UserAuthProvider toLocalAuthProvider(Users user) {

        UserAuthProvider authProvider = new UserAuthProvider();

        authProvider.setUser(user);
        authProvider.setProvider(AuthProvider.LOCAL);

        // Set this after the user has been persisted.
        authProvider.setProviderId(user.getId().toString());

        return authProvider;
    }
}
