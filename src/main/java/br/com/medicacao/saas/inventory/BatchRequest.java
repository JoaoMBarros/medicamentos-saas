package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BatchRequest(
        @NotNull UUID medicationId,
        @NotNull UUID locationId,
        @NotBlank @Size(max = 100) String lotNumber,
        @NotNull @FutureOrPresent LocalDate expirationDate,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity) {
}
