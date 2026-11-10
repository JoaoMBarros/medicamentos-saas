package br.com.medicacao.saas.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipRole;
import br.com.medicacao.saas.organization.MembershipStatus;
import br.com.medicacao.saas.organization.OrganizationMembership;

@ExtendWith(MockitoExtension.class)
class LocationServiceTests {

    @Mock LocationRepository locations;
    @Mock MembershipRepository memberships;

    private final UUID organizationId = UUID.randomUUID();
    private LocationService service;

    @BeforeEach
    void setUp() {
        service = new LocationService(locations, memberships, new TenantContext());
        authenticate("user-1", organizationId.toString());
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsLocationForCurrentTenant() {
        LocationRequest request = new LocationRequest("Main pharmacy", LocationType.PHARMACY);
        when(locations.existsByOrganizationIdAndNameAndType(organizationId,
                "main pharmacy", LocationType.PHARMACY)).thenReturn(false);
        when(locations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocationResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("main pharmacy");
        assertThat(response.type()).isEqualTo(LocationType.PHARMACY);
        verify(locations).save(argThat(location ->
                location.getOrganizationId().equals(organizationId)));
    }

    @Test
    void rejectsDuplicateLocation() {
        LocationRequest request = new LocationRequest("Main pharmacy", LocationType.PHARMACY);
        when(locations.existsByOrganizationIdAndNameAndType(organizationId,
                "main pharmacy", LocationType.PHARMACY)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(LocationConflictException.class);
    }

    @Test
    void updatesAndSearchesLocationsWithinTenant() {
        UUID locationId = UUID.randomUUID();
        Location location = new Location(organizationId, "Main pharmacy", LocationType.PHARMACY);
        when(locations.findByIdAndOrganizationId(locationId, organizationId))
                .thenReturn(Optional.of(location));
        when(locations.existsByOrganizationIdAndNameAndTypeAndIdNot(organizationId,
                "central warehouse", LocationType.WAREHOUSE, locationId)).thenReturn(false);
        PageRequest page = PageRequest.of(0, 20);
        when(locations.search(organizationId, "central", page))
                .thenReturn(new PageImpl<>(java.util.List.of(location), page, 1));

        assertThat(service.update(locationId,
                new LocationRequest("Central warehouse", LocationType.WAREHOUSE)).type())
                .isEqualTo(LocationType.WAREHOUSE);
        assertThat(service.search(" central ", page).content()).hasSize(1);
        verify(locations).search(organizationId, "central", page);
    }

    @Test
    void rejectsInactiveMemberAndMissingLocation() {
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.search(null, PageRequest.of(0, 20)))
                .isInstanceOf(TenantAccessDeniedException.class);

        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
        when(locations.findByIdAndOrganizationId(any(), eq(organizationId)))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(UUID.randomUUID()))
                .isInstanceOf(LocationNotFoundException.class);
    }

    private void authenticate(String subject, String organization) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(subject)
                .claim("preferred_username", "operator").claim("organization_id", organization)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
