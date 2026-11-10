package br.com.medicacao.saas.organization;

import java.time.Clock;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.identity.AuthenticatedUser;
import br.com.medicacao.saas.identity.TenantContext;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final OrganizationAuditRepository auditEvents;
    private final TenantContext tenantContext;
    private final Clock clock;

    @Transactional
    public OrganizationResponse create(OrganizationRequest request) {
        AuthenticatedUser actor = tenantContext.currentUser();
        if (organizations.existsBySlug(request.slug())) {
            throw new OrganizationConflictException("Organization slug is already in use.");
        }
        Organization organization = organizations.save(new Organization(request.name(), request.slug()));
        memberships.save(new OrganizationMembership(organization.getId(), actor.subject(),
                actor.username(), MembershipRole.OWNER, MembershipStatus.ACTIVE));
        auditEvents.save(new OrganizationAuditEvent(organization.getId(), actor.subject(),
                "ORGANIZATION_CREATED", actor.subject(), organization.getSlug(), clock));
        return OrganizationResponse.from(organization);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse get(UUID organizationId) {
        tenantContext.requireOrganization(organizationId.toString());
        return OrganizationResponse.from(findOrganization(organizationId));
    }

    @Transactional
    public OrganizationResponse suspend(UUID organizationId) {
        requireAdministrator(organizationId);
        Organization organization = findOrganization(organizationId);
        organization.suspend();
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "ORGANIZATION_SUSPENDED", null, null, clock));
        return OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationResponse archive(UUID organizationId) {
        requireAdministrator(organizationId);
        Organization organization = findOrganization(organizationId);
        organization.archive();
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "ORGANIZATION_ARCHIVED", null, null, clock));
        return OrganizationResponse.from(organization);
    }

    @Transactional(readOnly = true)
    public MembershipPageResponse listMemberships(UUID organizationId, Pageable pageable) {
        requireMember(organizationId);
        return MembershipPageResponse.from(memberships.findAllByOrganizationIdAndStatusNot(
                organizationId, MembershipStatus.REMOVED, pageable));
    }

    @Transactional
    public MembershipResponse invite(UUID organizationId, MembershipRequest request) {
        requireAdministrator(organizationId);
        if (memberships.findByOrganizationIdAndUserId(organizationId, request.userId()).isPresent()) {
            throw new OrganizationConflictException("User already has a membership in this organization.");
        }
        OrganizationMembership membership = memberships.save(new OrganizationMembership(organizationId,
                request.userId(), request.username(), request.role(), MembershipStatus.INVITED));
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "MEMBERSHIP_INVITED", request.userId(), request.role().name(), clock));
        return MembershipResponse.from(membership);
    }

    @Transactional
    public MembershipResponse changeRole(UUID organizationId, UUID membershipId, RoleRequest request) {
        requireAdministrator(organizationId);
        OrganizationMembership membership = findMembership(organizationId, membershipId);
        if (membership.getRole() == MembershipRole.OWNER && request.role() != MembershipRole.OWNER) {
            throw new OrganizationConflictException("The organization owner role cannot be changed.");
        }
        membership.changeRole(request.role());
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "MEMBERSHIP_ROLE_CHANGED", membership.getUserId(), request.role().name(), clock));
        return MembershipResponse.from(membership);
    }

    @Transactional
    public void remove(UUID organizationId, UUID membershipId) {
        requireAdministrator(organizationId);
        OrganizationMembership membership = findMembership(organizationId, membershipId);
        if (membership.getRole() == MembershipRole.OWNER) {
            throw new OrganizationConflictException("The organization owner cannot be removed.");
        }
        membership.remove();
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "MEMBERSHIP_REMOVED", membership.getUserId(), null, clock));
    }

    private Organization findOrganization(UUID id) {
        return organizations.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization was not found."));
    }

    private OrganizationMembership findMembership(UUID organizationId, UUID membershipId) {
        OrganizationMembership membership = memberships.findByIdAndOrganizationId(membershipId, organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Membership was not found."));
        return membership;
    }

    private OrganizationMembership requireMember(UUID organizationId) {
        tenantContext.requireOrganization(organizationId.toString());
        AuthenticatedUser actor = tenantContext.currentUser();
        return memberships.findByOrganizationIdAndUserId(organizationId, actor.subject())
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new br.com.medicacao.saas.identity.TenantAccessDeniedException(
                        "The user is not an active member of this organization."));
    }

    private void requireAdministrator(UUID organizationId) {
        MembershipRole role = requireMember(organizationId).getRole();
        if (role != MembershipRole.OWNER && role != MembershipRole.ADMIN) {
            throw new br.com.medicacao.saas.identity.TenantAccessDeniedException(
                    "Administrator permission is required.");
        }
    }
}
