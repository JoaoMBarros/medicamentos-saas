package br.com.medicacao.saas.inventory;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class StockBatchControllerTests {

    private final StockBatchService batches = mock(StockBatchService.class);
    private final InventoryMovementService movements = mock(InventoryMovementService.class);
    private final StockBatchController controller = new StockBatchController(batches, movements);

    @Test
    void delegatesBatchAndMovementOperations() {
        UUID batchId = UUID.randomUUID();
        BatchRequest batch = new BatchRequest(UUID.randomUUID(), UUID.randomUUID(), "LOT-1",
                LocalDate.of(2026, 12, 1), new BigDecimal("5"));
        MovementRequest movement = new MovementRequest(MovementType.ADDITION,
                new BigDecimal("1"), null);
        PageRequest page = PageRequest.of(0, 20);

        controller.create("batch-key", batch);
        controller.get(batchId);
        controller.update(batchId, batch);
        controller.search(null, null, null, page);
        controller.expired(page);
        controller.expiring(30, page);
        controller.move(batchId, "movement-key", movement);
        controller.history(batchId);

        verify(batches).create(batch, "batch-key");
        verify(batches).get(batchId);
        verify(batches).update(batchId, batch);
        verify(batches).search(null, null, null, page);
        verify(batches).expired(page);
        verify(batches).expiring(30, page);
        verify(movements).create(batchId, movement, "movement-key");
        verify(movements).history(batchId);
    }
}
