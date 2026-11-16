
package com.nikkssy.library_management.services.helper.security.jwt;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.model.CustomUserDetails;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        Users user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                        usernameOrEmail,
                        usernameOrEmail
                )
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid credentials")
                );

        boolean hasLocalProvider = userAuthProviderRepository
                .existsByUserIdAndProvider(user.getId(), AuthProvider.LOCAL);

        if (!hasLocalProvider) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return new CustomUserDetails(user);
    }
}
