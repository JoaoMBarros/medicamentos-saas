package br.com.medicacao.saas.catalog;

import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipStatus;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locations;
    private final MembershipRepository memberships;
    private final TenantContext tenantContext;

    @Transactional
    public LocationResponse create(LocationRequest request) {
        UUID organizationId = requireMember();
        if (isDuplicate(organizationId, request)) {
            throw new LocationConflictException("Location already exists in this organization.");
        }
        return LocationResponse.from(locations.save(
                new Location(organizationId, request.name(), request.type())));
    }

    @Transactional(readOnly = true)
    public LocationResponse get(UUID locationId) {
        return LocationResponse.from(findLocation(locationId, requireMember()));
    }

    @Transactional
    public LocationResponse update(UUID locationId, LocationRequest request) {
        UUID organizationId = requireMember();
        Location location = findLocation(locationId, organizationId);
        if (locations.existsByOrganizationIdAndNameAndTypeAndIdNot(organizationId,
                normalize(request.name()), request.type(), locationId)) {
            throw new LocationConflictException("Location already exists in this organization.");
        }
        location.update(request.name(), request.type());
        return LocationResponse.from(location);
    }

    @Transactional(readOnly = true)
    public LocationPageResponse search(String term, Pageable pageable) {
        UUID organizationId = requireMember();
        String normalizedTerm = term == null || term.isBlank()
                ? null
                : term.trim().toLowerCase(Locale.ROOT);
        Page<Location> result = locations.search(organizationId, normalizedTerm, pageable);
        return LocationPageResponse.from(result);
    }

    private Location findLocation(UUID locationId, UUID organizationId) {
        return locations.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new LocationNotFoundException("Location was not found."));
    }

    private boolean isDuplicate(UUID organizationId, LocationRequest request) {
        return locations.existsByOrganizationIdAndNameAndType(organizationId,
                normalize(request.name()), request.type());
    }

    private UUID requireMember() {
        String organization = tenantContext.currentUser().organizationId();
        if (organization == null) {
            throw new TenantAccessDeniedException("An organization context is required.");
        }
        UUID organizationId = UUID.fromString(organization);
        memberships.findByOrganizationIdAndUserId(organizationId, tenantContext.currentUser().subject())
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new TenantAccessDeniedException(
                        "The user is not an active member of this organization."));
        return organizationId;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
