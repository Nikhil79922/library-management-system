package com.nikkssy.library_management.mapper.auth;

import com.nikkssy.library_management.dto.auth.response.AuthResponse;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final UserMapper userMapper;

    public AuthResponse toAuthResponse(
            String accessToken,
            Users user
    ) {
        return new AuthResponse(
                accessToken,
                "Bearer",
                userMapper.toResponse(user)
        );
    }
}