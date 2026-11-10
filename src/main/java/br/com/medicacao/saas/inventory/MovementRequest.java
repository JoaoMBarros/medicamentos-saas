package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MovementRequest(
        @NotNull MovementType type,
        @NotNull BigDecimal quantity,
        UUID targetLocationId) {
}
