package br.com.medicacao.saas.inventory;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class StockBatchController {

    private final StockBatchService batches;
    private final InventoryMovementService movements;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BatchResponse create(@RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey, @Valid @RequestBody BatchRequest request) {
        return batches.create(request, idempotencyKey);
    }

    @GetMapping("/{batchId}")
    BatchResponse get(@PathVariable UUID batchId) {
        return batches.get(batchId);
    }

    @PutMapping("/{batchId}")
    BatchResponse update(@PathVariable UUID batchId, @Valid @RequestBody BatchRequest request) {
        return batches.update(batchId, request);
    }

    @GetMapping
    BatchPageResponse search(@RequestParam(required = false) UUID medicationId,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) BatchStatus status,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        return batches.search(medicationId, locationId, status, pageable);
    }

    @GetMapping("/expired")
    BatchPageResponse expired(@PageableDefault(size = 20, sort = "expirationDate")
            Pageable pageable) {
        return batches.expired(pageable);
    }

    @GetMapping("/expiring")
    BatchPageResponse expiring(@RequestParam(defaultValue = "30") int days,
            @PageableDefault(size = 20, sort = "expirationDate") Pageable pageable) {
        return batches.expiring(days, pageable);
    }

    @PostMapping("/{batchId}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    MovementResponse move(@PathVariable UUID batchId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody MovementRequest request) {
        return movements.create(batchId, request, idempotencyKey);
    }

    @GetMapping("/{batchId}/movements")
    java.util.List<MovementResponse> history(@PathVariable UUID batchId) {
        return movements.history(batchId);
    }
}
