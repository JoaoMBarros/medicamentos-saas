package br.com.medicacao.saas.catalog;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MedicationResponse create(@Valid @RequestBody MedicationRequest request) {
        return service.create(request);
    }

    @GetMapping("/{medicationId}")
    MedicationResponse get(@PathVariable UUID medicationId) {
        return service.get(medicationId);
    }

    @PutMapping("/{medicationId}")
    MedicationResponse update(@PathVariable UUID medicationId,
            @Valid @RequestBody MedicationRequest request) {
        return service.update(medicationId, request);
    }

    @GetMapping
    MedicationPageResponse search(@RequestParam(required = false) String term,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.search(term, pageable);
    }
}
