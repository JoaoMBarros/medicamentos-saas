package br.com.medicacao.saas.identity;

import java.util.Set;

public record AuthenticatedUser(
        String subject,
        String username,
        String organizationId,
        Set<String> roles) {
}
