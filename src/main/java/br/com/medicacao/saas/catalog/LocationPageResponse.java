package br.com.medicacao.saas.catalog;

import java.util.List;

import org.springframework.data.domain.Page;

public record LocationPageResponse(
        List<LocationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    static LocationPageResponse from(Page<Location> locations) {
        return new LocationPageResponse(
                locations.map(LocationResponse::from).getContent(),
                locations.getNumber(), locations.getSize(),
                locations.getTotalElements(), locations.getTotalPages());
    }
}
