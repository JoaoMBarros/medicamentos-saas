package br.com.medicacao.saas.organization;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationAuditRepository extends JpaRepository<OrganizationAuditEvent, UUID> {
}
