package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementResponse(
        UUID id,
        UUID batchId,
        MovementType type,
        BigDecimal quantity,
        UUID fromLocationId,
        UUID toLocationId,
        Instant createdAt) {

    static MovementResponse from(InventoryMovement movement) {
        return new MovementResponse(movement.getId(), movement.getBatchId(), movement.getType(),
                movement.getQuantity(), movement.getFromLocationId(), movement.getToLocationId(),
                movement.getCreatedAt());
    }
}
