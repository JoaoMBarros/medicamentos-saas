package br.com.medicacao.saas.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class TenantContextTests {

    private final TenantContext context = new TenantContext();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsAuthenticatedJwtClaims() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("user")
                .claim("preferred_username", "operator").claim("organization_id", "org")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(context.currentUser().subject()).isEqualTo("user");
        context.requireOrganization("org");
    }

    @Test
    void rejectsMissingAndCrossTenantAuthentication() {
        assertThatThrownBy(context::currentUser).isInstanceOf(TenantAuthenticationException.class);
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(Jwt.withTokenValue("token").header("alg", "none")
                        .subject("user").issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(60)).build(),
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThatThrownBy(() -> context.requireOrganization("org"))
                .isInstanceOf(TenantAccessDeniedException.class);
    }
}
