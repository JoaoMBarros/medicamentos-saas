package br.com.medicacao.saas.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Component
public class ApplicationJwtAuthenticationConverter
        implements Converter<Jwt, ApplicationJwtAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public ApplicationJwtAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        String subject = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String organizationId = jwt.getClaimAsString("organization_id");
        Set<String> roles = rolesFrom(jwt);

        AuthenticatedUser user = new AuthenticatedUser(
                subject,
                username == null ? subject : username,
                organizationId,
                roles);
        return new ApplicationJwtAuthenticationToken(jwt, authorities, user);
    }

    private Set<String> rolesFrom(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(roles));
    }
}
