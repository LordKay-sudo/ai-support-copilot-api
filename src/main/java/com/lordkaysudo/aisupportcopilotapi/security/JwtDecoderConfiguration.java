package com.lordkaysudo.aisupportcopilotapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Declares a symmetric {@link JwtDecoder} from the configured secret so the OAuth2 resource server
 * always has a decoder (avoids missing {@code JwtDecoder} when auto-configuration does not register one).
 */
@Configuration
public class JwtDecoderConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String rawSecret
    ) {
        byte[] keyBytes = rawSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "spring.security.oauth2.resourceserver.jwt.secret-key must be at least 32 bytes for HS256"
            );
        }
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
