package br.com.medicacao.saas.catalog;

import java.util.UUID;

public record MedicationResponse(
        UUID id,
        String name,
        String activeIngredient,
        String concentration,
        String presentation,
        String anvisaRegistration,
        String ean) {

    static MedicationResponse from(Medication medication) {
        return new MedicationResponse(medication.getId(), medication.getName(),
                medication.getActiveIngredient(), medication.getConcentration(),
                medication.getPresentation(), medication.getAnvisaRegistration(),
                medication.getEan());
    }
}
