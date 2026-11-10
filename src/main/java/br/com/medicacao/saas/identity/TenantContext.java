package br.com.medicacao.saas.identity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TenantContext {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new TenantAuthenticationException("Authentication is required.");
        }
        if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            var jwt = jwtAuthentication.getToken();
            String username = jwt.getClaimAsString("preferred_username");
            return new AuthenticatedUser(
                    jwt.getSubject(),
                    username == null ? jwt.getSubject() : username,
                    jwt.getClaimAsString("organization_id"),
                    java.util.Set.of());
        }
        throw new TenantAuthenticationException("Authentication does not contain an application identity.");
    }

    public void requireOrganization(String organizationId) {
        String currentOrganization = currentUser().organizationId();
        if (currentOrganization == null || !currentOrganization.equals(organizationId)) {
            throw new TenantAccessDeniedException("The requested resource belongs to another organization.");
        }
    }
}
