package br.com.medicacao.saas.organization;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<OrganizationMembership, UUID> {
    Page<OrganizationMembership> findAllByOrganizationIdAndStatusNot(UUID organizationId,
            MembershipStatus status, Pageable pageable);
    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, String userId);
    Optional<OrganizationMembership> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
