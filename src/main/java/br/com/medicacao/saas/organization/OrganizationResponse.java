package br.com.medicacao.saas.organization;

import java.util.UUID;

public record OrganizationResponse(UUID id, String name, String slug, OrganizationStatus status) {
    static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(organization.getId(), organization.getName(),
                organization.getSlug(), organization.getStatus());
    }
}
