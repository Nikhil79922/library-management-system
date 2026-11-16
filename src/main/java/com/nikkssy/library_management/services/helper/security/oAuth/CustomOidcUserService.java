
package com.nikkssy.library_management.services.helper.security.oAuth;

import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.mapper.UserMapper;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.repository.UserRepository;
import com.nikkssy.library_management.exception.OAuth2AuthenticationException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {

        OidcUser oidcUser = super.loadUser(userRequest);

        String googleSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String fullName = oidcUser.getFullName();

        // Validate Google identity information.
        if (googleSub == null ||
                email == null ||
                fullName == null ||
                !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {

            throw new OAuth2AuthenticationException(
                    "Google account must provide a verified email and valid identity"
            );
        }

        // 1. First check whether this Google identity already exists.
        UserAuthProvider existingProvider =
                userAuthProviderRepository
                        .findByProviderAndProviderId(
                                AuthProvider.GOOGLE,
                                googleSub
                        )
                        .orElse(null);

        Users user;

        if (existingProvider != null) {

            // Existing Google identity.
            user = existingProvider.getUser();

        } else {

            // 2. Find an existing account using the verified email.
            user = userRepository
                    .findByEmailIgnoreCase(email)
                    .orElseGet(() -> createGoogleUser(email, fullName));

            // Do not link disabled accounts.
            validateEnabledAccount(user);

            // 3. Link the Google identity to the resolved user.
            UserAuthProvider googleProvider =
                    userMapper.toGoogleAuthProvider(user, googleSub);

            userAuthProviderRepository.save(googleProvider);
        }

        // 4. Validate the resolved account.
        validateEnabledAccount(user);

        return oidcUser;
    }

    private Users createGoogleUser(String email, String fullName) {

        String username = generateUniqueUsername(email);

        Users user = userMapper.toGoogleUser(
                fullName,
                email,
                username
        );

        return userRepository.save(user);
    }

    private void validateEnabledAccount(Users user) {

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new OAuth2AuthenticationException(
                    "Your account is disabled"
            );
        }
    }

    private String generateUniqueUsername(String email) {

        String localPart = email.substring(0, email.indexOf('@'))
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_+", "_");

        if (localPart.length() < 3) {
            localPart = "usr_" + localPart;
        }

        localPart = localPart.substring(
                0,
                Math.min(localPart.length(), 15)
        );

        String username = localPart;

        while (userRepository.existsByUsername(username)) {

            username = localPart + "_" +
                    UUID.randomUUID().toString().substring(0, 4);
        }

        return username;
    }
}
