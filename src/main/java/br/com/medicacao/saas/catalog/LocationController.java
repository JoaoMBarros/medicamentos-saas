package br.com.medicacao.saas.catalog;

import java.util.UUID;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    LocationResponse create(@Valid @RequestBody LocationRequest request) {
        return service.create(request);
    }

    @GetMapping("/{locationId}")
    LocationResponse get(@PathVariable UUID locationId) {
        return service.get(locationId);
    }

    @PutMapping("/{locationId}")
    LocationResponse update(@PathVariable UUID locationId,
            @Valid @RequestBody LocationRequest request) {
        return service.update(locationId, request);
    }

    @GetMapping
    LocationPageResponse search(@RequestParam(required = false) String term,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.search(term, pageable);
    }
}
