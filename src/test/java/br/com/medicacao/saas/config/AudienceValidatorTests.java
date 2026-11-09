package br.com.medicacao.saas.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;

class AudienceValidatorTests {

    private final AudienceValidator validator = new AudienceValidator("medicacao-api");

    @Test
    void acceptsTokenWithExpectedAudience() {
        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of("medicacao-api")));

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWithoutExpectedAudience() {
        OAuth2TokenValidatorResult result = validator.validate(jwt(List.of("other-api")));

        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void rejectsExpiredToken() {
        Instant now = Instant.now();
        Jwt expiredToken = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .issuer("http://test-issuer")
                .audience(List.of("medicacao-api"))
                .issuedAt(now.minusSeconds(600))
                .expiresAt(now.minusSeconds(300))
                .build();

        assertThat(JwtValidators.createDefaultWithIssuer("http://test-issuer")
                .validate(expiredToken)
                .hasErrors()).isTrue();
    }

    private Jwt jwt(List<String> audience) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("user-123")
                .issuer("http://test-issuer")
                .audience(audience)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
    }
}
