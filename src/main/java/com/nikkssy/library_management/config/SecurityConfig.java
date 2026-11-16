package com.nikkssy.library_management.config;

import com.nikkssy.library_management.security.handler.CustomAccessDeniedHandler;
import com.nikkssy.library_management.security.handler.CustomAuthenticationEntryPoint;
import com.nikkssy.library_management.services.helper.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;


import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

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
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Qualifier("auth-jwtDecoder") JwtDecoder jwtDecoder
//            GoogleAuthenticationSuccessHandler googleAuthenticationSuccessHandler
    )
            throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(
                                        "/api/auth/signup",
                                        "/api/auth/login",
                                        "/api/auth/refresh-token",
                                        "/api/auth/logout",
                                        "/api/auth/forgot-password",
                                        "/api/auth/reset-password"
                                ).permitAll()

                                .requestMatchers("/api/auth/logout-all")
                                .authenticated()

                                        .requestMatchers("/api/admin/**")
                                        .hasRole("ADMIN")

                                        .requestMatchers("/api/**")
                                        .authenticated()

                                        .anyRequest()
                                        .permitAll()
                )

                 //Custom Exception handlers
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

// JWT validation for API requests
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .cors(cors ->
                        cors.configurationSource(corsConfigurationSource())
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
                return null;
            }
        };
    }


    // =========================
    // JWT Secret Key
    // =========================

    @Bean("auth-jwtSecretKey")
    public SecretKey jwtSecretKey(
            @Value("${jwt.secret}") String secret
    ) {

        byte[] decodedKey = Base64.getDecoder()
                .decode(secret);

        return new SecretKeySpec(
                decodedKey,
                "HmacSHA256"
        );
    }


    // =========================
    // JWT Encoder
    // =========================

    @Bean("auth-jwtEncoder")
    public JwtEncoder jwtEncoder(@Qualifier("auth-jwtSecretKey") SecretKey secretKey) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    // =========================
    // JWT Decoder
    // =========================

    @Bean("auth-jwtDecoder")
    public JwtDecoder jwtDecoder(
            @Qualifier("auth-jwtSecretKey") SecretKey secretKey,
            @Value("${jwt.issuer}") String issuer
    ) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
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