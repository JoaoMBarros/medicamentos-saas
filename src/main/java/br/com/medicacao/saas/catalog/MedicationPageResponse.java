package br.com.medicacao.saas.catalog;

import java.util.List;

import org.springframework.data.domain.Page;

public record MedicationPageResponse(
        List<MedicationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static MedicationPageResponse from(Page<Medication> medications) {
        return new MedicationPageResponse(
                medications.map(MedicationResponse::from).getContent(),
                medications.getNumber(), medications.getSize(),
                medications.getTotalElements(), medications.getTotalPages());
    }
}
