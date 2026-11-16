package com.nikkssy.library_management.services.businessLogic.auth;

import com.nikkssy.library_management.entity.Users;
import com.nikkssy.library_management.repository.UserRepository;
import com.nikkssy.library_management.services.impl.auth.PasswordResetTokenServiceImpl;
import com.nikkssy.library_management.exception.IllegalArgumentException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetTokenService implements PasswordResetTokenServiceImpl {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Value("${app.security.password-reset.issuer}")
    private String issuer;
    @Value("${app.security.password-reset.expiration}")
    private long expiration;

    public PasswordResetTokenService(
            @Qualifier("passwordResetJwtEncoder")
            JwtEncoder jwtEncoder,
            @Qualifier("passwordResetJwtDecoder")
            JwtDecoder jwtDecoder,
            UserRepository userRepository
    ){
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
    }


    @Override
    public String generateResetToken(Users user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .id(UUID.randomUUID().toString())
                .claim("purpose", "PASSWORD_RESET")
                .build();

        JwsHeader jwsHeader = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }

    @Override
    public Users validateResetToken(String token) {

        try {
            Jwt jwt = jwtDecoder.decode(token);

            String purpose = jwt.getClaimAsString("purpose");

            if (!"PASSWORD_RESET".equals(purpose)) {
                throw new IllegalArgumentException(
                        "Invalid password reset token"
                );
            }

            Long userId = Long.parseLong(jwt.getSubject());

            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Invalid password reset token"
                    ));

            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new IllegalArgumentException(
                        "Invalid password reset token"
                );
            }

            Instant issuedAt = jwt.getIssuedAt();

            if (issuedAt == null) {
                throw new IllegalArgumentException(
                        "Invalid password reset token"
                );
            }

            Instant passwordChangedAt = user.getPasswordChangedAt();

            if (passwordChangedAt != null &&
                    issuedAt.getEpochSecond() <=
                            passwordChangedAt.getEpochSecond()) {

                throw new IllegalArgumentException(
                        "Password reset token is no longer valid"
                );
            }

            return user;

        } catch (JwtException | NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid or expired password reset token"
            );
        }
    }
}
