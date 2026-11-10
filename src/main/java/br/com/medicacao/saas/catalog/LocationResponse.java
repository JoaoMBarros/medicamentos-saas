package br.com.medicacao.saas.catalog;

import java.util.UUID;

public record LocationResponse(UUID id, String name, LocationType type) {

    static LocationResponse from(Location location) {
        return new LocationResponse(location.getId(), location.getName(), location.getType());
    }
}
