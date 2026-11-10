package br.com.medicacao.saas.organization;

import java.util.List;

import org.springframework.data.domain.Page;

public record MembershipPageResponse(
        List<MembershipResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static MembershipPageResponse from(Page<OrganizationMembership> memberships) {
        return new MembershipPageResponse(
                memberships.map(MembershipResponse::from).getContent(),
                memberships.getNumber(),
                memberships.getSize(),
                memberships.getTotalElements(),
                memberships.getTotalPages());
    }
}
