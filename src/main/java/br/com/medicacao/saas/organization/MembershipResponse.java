package br.com.medicacao.saas.organization;

import java.util.UUID;

public record MembershipResponse(UUID id, String userId, String username, MembershipRole role,
        MembershipStatus status) {
    static MembershipResponse from(OrganizationMembership membership) {
        return new MembershipResponse(membership.getId(), membership.getUserId(),
                membership.getUsername(), membership.getRole(), membership.getStatus());
    }
}
