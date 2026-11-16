package com.nikkssy.library_management.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
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

}