package com.nikkssy.library_management.services.helper.security.jwt;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiry}")
    private long expiry;

    public JwtService( @Qualifier("auth-jwtEncoder") JwtEncoder jwtEncoder){
        this.jwtEncoder = jwtEncoder;
    }

    // Login: uses authenticated Authentication
    public String generateToken(Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_")
                        ? authority.substring(5)
                        : authority)
                .toList();

        return generateToken(
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles
        );
    }

    // Refresh: uses user retrieved after refresh-token validation
    public String generateToken(Users user) {

        List<String> roles = List.of(user.getRole().name());

        return generateToken(
                user.getUsername(),
                user.getEmail(),
                roles
        );
    }



    // Common JWT generation logic
    private String generateToken(
            String username,
            String email,
            List<String> roles
    ) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .claim("email", email)
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }
}