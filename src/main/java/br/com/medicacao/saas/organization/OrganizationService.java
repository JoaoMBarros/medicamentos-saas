package br.com.medicacao.saas.organization;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.identity.AuthenticatedUser;
import br.com.medicacao.saas.identity.TenantContext;

@Service
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final MembershipRepository memberships;
    private final OrganizationAuditRepository auditEvents;
    private final TenantContext tenantContext;

    public OrganizationService(OrganizationRepository organizations, MembershipRepository memberships,
            OrganizationAuditRepository auditEvents, TenantContext tenantContext) {
        this.organizations = organizations;
        this.memberships = memberships;
        this.auditEvents = auditEvents;
        this.tenantContext = tenantContext;
    }

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
                "ORGANIZATION_CREATED", actor.subject(), organization.getSlug()));
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
                "ORGANIZATION_SUSPENDED", null, null));
        return OrganizationResponse.from(organization);
    }

    @Transactional
    public OrganizationResponse archive(UUID organizationId) {
        requireAdministrator(organizationId);
        Organization organization = findOrganization(organizationId);
        organization.archive();
        auditEvents.save(new OrganizationAuditEvent(organizationId, tenantContext.currentUser().subject(),
                "ORGANIZATION_ARCHIVED", null, null));
        return OrganizationResponse.from(organization);
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> listMemberships(UUID organizationId) {
        requireMember(organizationId);
        return memberships.findAllByOrganizationIdAndStatusNot(organizationId, MembershipStatus.REMOVED)
                .stream().map(MembershipResponse::from).toList();
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
                "MEMBERSHIP_INVITED", request.userId(), request.role().name()));
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
                "MEMBERSHIP_ROLE_CHANGED", membership.getUserId(), request.role().name()));
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
                "MEMBERSHIP_REMOVED", membership.getUserId(), null));
    }

    private Organization findOrganization(UUID id) {
        return organizations.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization was not found."));
    }

    private OrganizationMembership findMembership(UUID organizationId, UUID membershipId) {
        OrganizationMembership membership = memberships.findById(membershipId)
                .filter(item -> item.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new OrganizationNotFoundException("Membership was not found."));
        return membership;
    }

    private void requireMember(UUID organizationId) {
        tenantContext.requireOrganization(organizationId.toString());
        AuthenticatedUser actor = tenantContext.currentUser();
        memberships.findByOrganizationIdAndUserId(organizationId, actor.subject())
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new br.com.medicacao.saas.identity.TenantAccessDeniedException(
                        "The user is not an active member of this organization."));
    }

    private void requireAdministrator(UUID organizationId) {
        requireMember(organizationId);
        MembershipRole role = memberships.findByOrganizationIdAndUserId(organizationId,
                tenantContext.currentUser().subject()).orElseThrow().getRole();
        if (role != MembershipRole.OWNER && role != MembershipRole.ADMIN) {
            throw new br.com.medicacao.saas.identity.TenantAccessDeniedException(
                    "Administrator permission is required.");
        }
    }
}
