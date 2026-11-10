package br.com.medicacao.saas.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<OrganizationMembership, UUID> {
    List<OrganizationMembership> findAllByOrganizationIdAndStatusNot(UUID organizationId,
            MembershipStatus status);
    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, String userId);
}
