package br.com.medicacao.saas.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class ApplicationJwtAuthenticationConverterTests {

    private final ApplicationJwtAuthenticationConverter converter =
            new ApplicationJwtAuthenticationConverter();

    @Test
    void mapsIdentityAndOrganizationClaims() {
        ApplicationJwtAuthenticationToken token = converter.convert(jwt(
                "user-123",
                "operator",
                "org-123",
                List.of("OPERATOR"),
                "read"));

        AuthenticatedUser user = (AuthenticatedUser) token.getPrincipal();
        assertThat(user.subject()).isEqualTo("user-123");
        assertThat(user.username()).isEqualTo("operator");
        assertThat(user.organizationId()).isEqualTo("org-123");
        assertThat(user.roles()).containsExactly("OPERATOR");
        assertThat(token.getAuthorities()).extracting("authority").containsExactly("SCOPE_read");
    }

    @Test
    void usesSubjectWhenUsernameAndRolesAreMissing() {
        ApplicationJwtAuthenticationToken token = converter.convert(
                jwt("user-123", null, null, null, null));

        AuthenticatedUser user = (AuthenticatedUser) token.getPrincipal();
        assertThat(user.username()).isEqualTo("user-123");
        assertThat(user.organizationId()).isNull();
        assertThat(user.roles()).isEmpty();
    }

    private Jwt jwt(
            String subject,
            String username,
            String organizationId,
            List<String> roles,
            String scope) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        if (username != null) {
            builder.claim("preferred_username", username);
        }
        if (organizationId != null) {
            builder.claim("organization_id", organizationId);
        }
        if (roles != null) {
            builder.claim("roles", roles);
        }
        if (scope != null) {
            builder.claim("scope", scope);
        }
        return builder.build();
    }
}
