package com.nikkssy.library_management.security.oAuthHandler;
import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.entity.UserAuthProvider;
import com.nikkssy.library_management.entity.enums.AuthProvider;
import com.nikkssy.library_management.exception.OAuth2AuthenticationException;
import com.nikkssy.library_management.repository.UserAuthProviderRepository;
import com.nikkssy.library_management.services.helper.cookies.AuthCookieManagement;
import com.nikkssy.library_management.services.helper.security.jwt.JwtService;

import com.nikkssy.library_management.services.impl.auth.RefreshTokenServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GoogleAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final AuthCookieManagement authCookieManagement;
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String googleSub = oidcUser.getSubject();

        // Resolve application user using Google's unique subject.
        UserAuthProvider googleProvider = userAuthProviderRepository
                .findByProviderAndProviderId(AuthProvider.GOOGLE, googleSub)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "Google account is not linked to an application user"
                ));

        Users user = googleProvider.getUser();

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new OAuth2AuthenticationException(
                    "Your account is disabled"
            );
        }

// Stop users whose phone number is not verified.
        if (!user.isPhoneVerified()) {

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendBaseUrl)
                    .path("/oauth2/complete-registration")
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
            return;
        }
        // Generate access token.
        String accessToken = jwtService.generateToken(user);

        // Generate refresh token.
        var refreshTokenResponse =
                refreshTokenService.createRefreshToken(user);

        // Set refresh token as HttpOnly cookie.
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                authCookieManagement.createRefreshTokenCookie(
                        refreshTokenResponse.refreshToken(),
                        refreshTokenResponse.expiryDate()
                ).toString()
        );

        // Redirect to frontend callback with access token in URL fragment.
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendBaseUrl)
                .path("/oauth2/callback")
                .fragment("accessToken=" + accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
