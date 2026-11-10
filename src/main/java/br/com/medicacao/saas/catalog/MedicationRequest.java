package br.com.medicacao.saas.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MedicationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 160) String activeIngredient,
        @NotBlank @Size(max = 80) String concentration,
        @NotBlank @Size(max = 120) String presentation,
        @Size(max = 40) String anvisaRegistration,
        @Pattern(regexp = "\\d{8,14}", message = "must contain 8 to 14 digits")
        String ean) {
}
