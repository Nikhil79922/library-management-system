
package com.nikkssy.library_management.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class PasswordResetJwtConfig {

    @Bean("passwordResetSecretKey")
    public SecretKey passwordResetSecretKey(
            @Value("${app.security.password-reset.secret}") String secret
    ) {
        byte[] decodedSecret = Base64.getDecoder().decode(secret);

        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException(
                    "Password reset JWT secret must contain at least 32 bytes"
            );
        }

        return new SecretKeySpec(decodedSecret, "HmacSHA256");
    }

    @Bean("passwordResetJwtEncoder")
    public JwtEncoder passwordResetJwtEncoder(
            @Qualifier("passwordResetSecretKey") SecretKey secretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean("passwordResetJwtDecoder")
    public JwtDecoder passwordResetJwtDecoder(
            @Qualifier(
                    "passwordResetSecretKey"
            ) SecretKey secretKey,
            @Value("${app.security.password-reset.issuer}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );

        return decoder;
    }
}
