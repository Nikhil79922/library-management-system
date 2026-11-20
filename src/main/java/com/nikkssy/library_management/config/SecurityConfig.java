package com.nikkssy.library_management.config;

import com.nikkssy.library_management.security.jwthandler.CustomAccessDeniedHandler;
import com.nikkssy.library_management.security.jwthandler.CustomAuthenticationEntryPoint;
import com.nikkssy.library_management.security.oAuthHandler.GoogleAuthFailureHandler;
import com.nikkssy.library_management.security.oAuthHandler.GoogleAuthSuccessHandler;
import com.nikkssy.library_management.services.helper.security.jwt.CustomUserDetailsService;
import com.nikkssy.library_management.services.helper.security.oAuth.CustomOidcUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomOidcUserService customOidcUserService;
    private final GoogleAuthSuccessHandler googleAuthSuccessHandler;
    private final GoogleAuthFailureHandler googleAuthFailureHandler;

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Qualifier("auth-jwtDecoder") JwtDecoder jwtDecoder
    ) throws Exception {

        return http

                // JWT API: stateless authentication.
                .csrf(csrf -> csrf.disable())

                .authenticationProvider(authenticationProvider)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh-token",
                                "/api/auth/logout",

                                // Password reset
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",

                                // Local email and phone OTP
                                "/api/auth/otp/email/verify",
                                "/api/auth/otp/phone/send",
                                "/api/auth/otp/phone/verify",

                                //OTP authentication
                                "/api/auth/otp/login/email/send",
                                "/api/auth/otp/login/email/verify",
                                "/api/auth/otp/login/phone/send",
                                "/api/auth/otp/login/phone/verify",

                                //Admin Login
                                "/api/admin/auth/login",
                                "/api/admin/auth/refresh-token",
                                "/api/admin/auth/logout"
                        ).permitAll()

                        // ==================================
                        // AUTHENTICATED ENDPOINTS
                        // ==================================

                        .requestMatchers(
                                "/api/auth/logout-all"
                        ).authenticated()

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/**"
                        ).authenticated()

                        .anyRequest().permitAll()
                )

                // Custom exception handlers.
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )

                        .accessDeniedHandler(
                                accessDeniedHandler
                        )
                )

                // JWT validation.
                .oauth2ResourceServer(oauth2 -> oauth2

                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )

                        .jwt(jwt -> jwt

                                .decoder(jwtDecoder)

                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .build();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain oauth2SecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http

                .securityMatcher(
                        "/oauth2/**",
                        "/login/oauth2/**",

                        // Google phone registration and verification
                        "/api/auth/phone/register",
                        "/api/auth/phone/verify"
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Google phone registration requires
                        // an authenticated Google session.
                        .requestMatchers(
                                "/api/auth/phone/register",
                                "/api/auth/phone/verify"
                        ).authenticated()

                        // OAuth2 authorization and callback endpoints.
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth2 -> oauth2

                        .userInfoEndpoint(userInfo ->
                                userInfo.oidcUserService(
                                        customOidcUserService
                                )
                        )

                        .successHandler(googleAuthSuccessHandler)

                        .failureHandler(googleAuthFailureHandler)
                )

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                .build();
    }

    public CorsConfigurationSource corsConfigurationSource(){
        return new CorsConfigurationSource() {
            @Override
            public  CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfs = new CorsConfiguration();
                cfs.setAllowCredentials(true);
                cfs.addAllowedOrigin("*");  // include the FE domain url
                cfs.setAllowedMethods(Collections.singletonList("*"));
                cfs.setAllowedHeaders(Collections.singletonList("*"));
                cfs.setExposedHeaders(Collections.singletonList("Authorization"));
                cfs.setMaxAge(3600L);
                return cfs;
            }
        };
    }



    // =========================
    // JWT Authorities Converter
    // =========================

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");

        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return converter;
    }
}