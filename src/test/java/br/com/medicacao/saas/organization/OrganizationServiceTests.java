package br.com.medicacao.saas.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTests {

    @Mock OrganizationRepository organizations;
    @Mock MembershipRepository memberships;
    @Mock OrganizationAuditRepository auditEvents;

    private OrganizationService service;
    private final UUID organizationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new OrganizationService(organizations, memberships, auditEvents, new TenantContext(),
                Clock.systemUTC());
        authenticate("user-1", organizationId.toString());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsOrganizationAndOwnerMembership() {
        when(organizations.existsBySlug("acme")).thenReturn(false);
        when(organizations.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberships.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrganizationResponse response = service.create(new OrganizationRequest("Acme", "acme"));

        assertThat(response.name()).isEqualTo("Acme");
        verify(memberships).save(argThat(m -> m.getRole() == MembershipRole.OWNER
                && m.getUserId().equals("user-1")));
        verify(auditEvents).save(any(OrganizationAuditEvent.class));
    }

    @Test
    void rejectsDuplicateSlug() {
        when(organizations.existsBySlug("acme")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new OrganizationRequest("Acme", "acme")))
                .isInstanceOf(OrganizationConflictException.class);
    }

    @Test
    void readsAndChangesOrganizationLifecycle() {
        Organization organization = organization();
        when(organizations.findById(organizationId)).thenReturn(Optional.of(organization));
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(activeMembership(MembershipRole.ADMIN)));

        assertThat(service.get(organizationId).status()).isEqualTo(OrganizationStatus.ACTIVE);
        assertThat(service.suspend(organizationId).status()).isEqualTo(OrganizationStatus.SUSPENDED);
        assertThat(service.archive(organizationId).status()).isEqualTo(OrganizationStatus.ARCHIVED);
        verify(auditEvents, times(2)).save(any(OrganizationAuditEvent.class));
    }

    @Test
    void listsAndInvitesMemberships() {
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(activeMembership(MembershipRole.OWNER)));
        when(memberships.findAllByOrganizationIdAndStatusNot(organizationId, MembershipStatus.REMOVED,
                PageRequest.of(0, 20))).thenReturn(new PageImpl<>(
                        List.of(activeMembership(MembershipRole.OWNER)), PageRequest.of(0, 20), 1));
        when(memberships.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-2"))
                .thenReturn(Optional.empty());

        assertThat(service.listMemberships(organizationId, PageRequest.of(0, 20)).content())
                .hasSize(1);
        MembershipResponse response = service.invite(organizationId,
                new MembershipRequest("user-2", "operator", MembershipRole.OPERATOR));

        assertThat(response.status()).isEqualTo(MembershipStatus.INVITED);
        verify(auditEvents).save(any(OrganizationAuditEvent.class));
    }

    @Test
    void changesRoleAndRemovesMembership() {
        OrganizationMembership membership = activeMembership(MembershipRole.OPERATOR);
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(activeMembership(MembershipRole.ADMIN)));
        when(memberships.findByIdAndOrganizationId(membership.getId(), organizationId))
                .thenReturn(Optional.of(membership));

        assertThat(service.changeRole(organizationId, membership.getId(),
                new RoleRequest(MembershipRole.VIEWER)).role()).isEqualTo(MembershipRole.VIEWER);
        service.remove(organizationId, membership.getId());
        assertThat(membership.getStatus()).isEqualTo(MembershipStatus.REMOVED);
        verify(auditEvents, times(2)).save(any(OrganizationAuditEvent.class));
    }

    @Test
    void rejectsCrossTenantAccessAndInactiveMembers() {
        UUID otherOrganization = UUID.randomUUID();
        assertThatThrownBy(() -> service.get(otherOrganization))
                .isInstanceOf(TenantAccessDeniedException.class);

        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(activeMembership(MembershipRole.VIEWER)));
        assertThatThrownBy(() -> service.invite(organizationId,
                new MembershipRequest("user-2", "operator", MembershipRole.OPERATOR)))
                .isInstanceOf(TenantAccessDeniedException.class);
    }

    @Test
    void rejectsMissingOrganizationAndProtectedOwnerOperations() {
        when(organizations.findById(organizationId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(organizationId))
                .isInstanceOf(OrganizationNotFoundException.class);

        OrganizationMembership owner = activeMembership(MembershipRole.OWNER);
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(owner));
        when(memberships.findByIdAndOrganizationId(owner.getId(), organizationId))
                .thenReturn(Optional.of(owner));
        assertThatThrownBy(() -> service.changeRole(organizationId, owner.getId(),
                new RoleRequest(MembershipRole.ADMIN)))
                .isInstanceOf(OrganizationConflictException.class);
        assertThatThrownBy(() -> service.remove(organizationId, owner.getId()))
                .isInstanceOf(OrganizationConflictException.class);
    }

    private Organization organization() {
        Organization organization = new Organization("Acme", "acme");
        setId(organization, organizationId);
        return organization;
    }

    private OrganizationMembership activeMembership(MembershipRole role) {
        OrganizationMembership membership = new OrganizationMembership(organizationId,
                "user-1", "operator", role, MembershipStatus.ACTIVE);
        setId(membership, UUID.randomUUID());
        return membership;
    }

    private void authenticate(String subject, String organization) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(subject)
                .claim("preferred_username", "operator").claim("organization_id", organization)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
