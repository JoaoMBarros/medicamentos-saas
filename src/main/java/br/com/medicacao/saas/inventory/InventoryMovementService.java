package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.catalog.LocationRepository;
import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipStatus;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {

    private final StockBatchRepository batches;
    private final InventoryMovementRepository movements;
    private final IdempotencyRecordRepository idempotency;
    private final LocationRepository locations;
    private final MembershipRepository memberships;
    private final TenantContext tenantContext;

    @Transactional
    public MovementResponse create(UUID batchId, MovementRequest request, String idempotencyKey) {
        UUID organizationId = requireMember();
        String key = requireKey(idempotencyKey);
        InventoryMovement existing = movements
                .findByOrganizationIdAndTypeAndIdempotencyKey(
                        organizationId, request.type(), key)
                .orElse(null);
        if (existing != null) {
            return MovementResponse.from(existing);
        }

        StockBatch batch = batches.findByIdAndOrganizationId(batchId, organizationId)
                .orElseThrow(() -> new InventoryNotFoundException("Batch was not found."));
        validateRequest(organizationId, batch, request);
        UUID fromLocationId = batch.getLocationId();
        UUID toLocationId = null;
        if (request.type() == MovementType.TRANSFER) {
            toLocationId = request.targetLocationId();
            batch.transferTo(toLocationId);
        } else if (request.type() == MovementType.ADDITION) {
            batch.add(requirePositive(request.quantity()));
        } else if (request.type() == MovementType.REMOVAL) {
            BigDecimal quantity = requirePositive(request.quantity());
            ensureNonNegative(batch.getQuantity().subtract(quantity));
            batch.remove(quantity);
        } else {
            BigDecimal adjustment = requireNonZero(request.quantity());
            ensureNonNegative(batch.getQuantity().add(adjustment));
            batch.adjust(adjustment);
        }

        InventoryMovement movement = movements.save(new InventoryMovement(organizationId, batchId,
                request.type(), request.quantity(), fromLocationId, toLocationId, key,
                tenantContext.currentUser().subject()));
        idempotency.save(new IdempotencyRecord(organizationId,
                "MOVEMENT_" + request.type().name(), key, movement.getId()));
        return MovementResponse.from(movement);
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> history(UUID batchId) {
        UUID organizationId = requireMember();
        batches.findByIdAndOrganizationId(batchId, organizationId)
                .orElseThrow(() -> new InventoryNotFoundException("Batch was not found."));
        return movements.findAllByOrganizationIdAndBatchIdOrderByCreatedAtAsc(
                        organizationId, batchId).stream()
                .map(MovementResponse::from)
                .toList();
    }

    private void validateRequest(UUID organizationId, StockBatch batch, MovementRequest request) {
        if (request.type() == MovementType.TRANSFER) {
            if (request.targetLocationId() == null) {
                throw new InventoryValidationException("Transfer target location is required.");
            }
            if (request.targetLocationId().equals(batch.getLocationId())) {
                throw new InventoryValidationException("Transfer target must differ from the current location.");
            }
            locations.findByIdAndOrganizationId(request.targetLocationId(), organizationId)
                    .orElseThrow(() -> new InventoryNotFoundException("Location was not found."));
            requirePositive(request.quantity());
        } else if (request.targetLocationId() != null) {
            throw new InventoryValidationException("Target location is only valid for transfers.");
        }
    }

    private BigDecimal requirePositive(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new InventoryValidationException("Quantity must be greater than zero.");
        }
        return quantity;
    }

    private BigDecimal requireNonZero(BigDecimal quantity) {
        if (quantity == null || quantity.signum() == 0) {
            throw new InventoryValidationException("Adjustment quantity cannot be zero.");
        }
        return quantity;
    }

    private void ensureNonNegative(BigDecimal quantity) {
        if (quantity.signum() < 0) {
            throw new InventoryValidationException("Stock quantity cannot become negative.");
        }
    }

    private String requireKey(String key) {
        if (key == null || key.isBlank() || key.length() > 200) {
            throw new InventoryValidationException("Idempotency-Key is required and must be at most 200 characters.");
        }
        return key.trim();
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
}
