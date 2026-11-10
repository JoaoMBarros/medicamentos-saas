package br.com.medicacao.saas.inventory;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByOrganizationIdAndOperationAndIdempotencyKey(
            UUID organizationId, String operation, String idempotencyKey);
}
