package com.lordkaysudo.aisupportcopilotapi.support;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * HS256 tokens for integration tests; must match {@code spring.security.oauth2.resourceserver.jwt.secret-key}.
 */
public final class TestJwtTokens {

    public static final String SECRET = "12345678901234567890123456789012";

    private TestJwtTokens() {
    }

    public static String agentToken() {
        return token(List.of("AGENT"));
    }

    public static String adminToken() {
        return token(List.of("ADMIN"));
    }

    private static String token(List<String> roles) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("integration-test")
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                    .claim("roles", roles)
                    .build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
