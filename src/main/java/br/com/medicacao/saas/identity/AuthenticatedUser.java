package br.com.medicacao.saas.identity;

import java.util.Set;

public record AuthenticatedUser(
        String subject,
        String username,
        String organizationId,
        Set<String> roles) {

    public AuthenticatedUser {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Authenticated user subject is required.");
        }
        username = username == null || username.isBlank() ? subject : username;
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
