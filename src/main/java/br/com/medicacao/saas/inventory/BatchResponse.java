package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatchResponse(
        UUID id,
        UUID medicationId,
        UUID locationId,
        String lotNumber,
        LocalDate expirationDate,
        BigDecimal quantity,
        BatchStatus status) {

    static BatchResponse from(StockBatch batch) {
        return new BatchResponse(batch.getId(), batch.getMedicationId(), batch.getLocationId(),
                batch.getLotNumber(), batch.getExpirationDate(), batch.getQuantity(),
                batch.getStatus());
    }
}
