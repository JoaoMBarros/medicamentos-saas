package br.com.medicacao.saas.inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    Optional<InventoryMovement> findByOrganizationIdAndTypeAndIdempotencyKey(
            UUID organizationId, MovementType type, String idempotencyKey);

    List<InventoryMovement> findAllByOrganizationIdAndBatchIdOrderByCreatedAtAsc(
            UUID organizationId, UUID batchId);
}
