package br.com.medicacao.saas.identity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class ApplicationJwtAuthenticationToken extends JwtAuthenticationToken {

    private final AuthenticatedUser authenticatedUser;

    public ApplicationJwtAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            AuthenticatedUser authenticatedUser) {
        super(jwt, authorities, authenticatedUser.username());
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public Object getPrincipal() {
        return authenticatedUser;
    }
}
