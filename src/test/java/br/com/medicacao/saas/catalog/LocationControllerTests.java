package br.com.medicacao.saas.catalog;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class LocationControllerTests {

    private final LocationService service = mock(LocationService.class);
    private final LocationController controller = new LocationController(service);

    @Test
    void delegatesLocationOperations() {
        UUID locationId = UUID.randomUUID();
        LocationRequest request = new LocationRequest("Main pharmacy", LocationType.PHARMACY);

        controller.create(request);
        controller.get(locationId);
        controller.update(locationId, request);
        controller.search("main", PageRequest.of(0, 20));

        verify(service).create(request);
        verify(service).get(locationId);
        verify(service).update(locationId, request);
        verify(service).search("main", PageRequest.of(0, 20));
    }
}
