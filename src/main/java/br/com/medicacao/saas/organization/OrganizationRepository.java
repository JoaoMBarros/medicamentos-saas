package br.com.medicacao.saas.organization;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsBySlug(String slug);
    Optional<Organization> findBySlug(String slug);
}
