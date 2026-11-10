package br.com.medicacao.saas.catalog;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class MedicationControllerTests {

    private final MedicationService service = mock(MedicationService.class);
    private final MedicationController controller = new MedicationController(service);

    @Test
    void delegatesMedicationOperations() {
        UUID medicationId = UUID.randomUUID();
        MedicationRequest request = new MedicationRequest("Name", "Ingredient", "10 mg",
                "Tablet", null, null);

        controller.create(request);
        controller.get(medicationId);
        controller.update(medicationId, request);
        controller.search("term", PageRequest.of(0, 20));

        verify(service).create(request);
        verify(service).get(medicationId);
        verify(service).update(medicationId, request);
        verify(service).search("term", PageRequest.of(0, 20));
    }
}
