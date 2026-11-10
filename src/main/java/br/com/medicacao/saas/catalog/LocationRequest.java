package br.com.medicacao.saas.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull LocationType type) {
}
