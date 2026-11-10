package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.catalog.LocationRepository;
import br.com.medicacao.saas.catalog.MedicationRepository;
import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipStatus;

@Service
@RequiredArgsConstructor
public class StockBatchService {

    private static final String BATCH_CREATE = "BATCH_CREATE";

    private final StockBatchRepository batches;
    private final InventoryMovementRepository movements;
    private final IdempotencyRecordRepository idempotency;
    private final MedicationRepository medications;
    private final LocationRepository locations;
    private final MembershipRepository memberships;
    private final TenantContext tenantContext;
    private final Clock clock;

    @Transactional
    public BatchResponse create(BatchRequest request, String idempotencyKey) {
        UUID organizationId = requireMember();
        String key = requireKey(idempotencyKey);
        var existing = idempotency.findByOrganizationIdAndOperationAndIdempotencyKey(
                organizationId, BATCH_CREATE, key);
        if (existing.isPresent()) {
            return get(existing.get().getResourceId());
        }
        validateReferences(organizationId, request.medicationId(), request.locationId());
        validateDateAndQuantity(request.expirationDate(), request.quantity());
        String lotNumber = request.lotNumber().trim();
        if (batches.existsByOrganizationIdAndMedicationIdAndLotNumber(
                organizationId, request.medicationId(), lotNumber)) {
            throw new InventoryConflictException("Batch already exists for this medication.");
        }
        StockBatch batch = batches.save(new StockBatch(organizationId, request.medicationId(),
                request.locationId(), lotNumber, request.expirationDate(), request.quantity()));
        batch.refreshStatus(today());
        InventoryMovement movement = movements.save(new InventoryMovement(organizationId,
                batch.getId(), MovementType.ADDITION, request.quantity(), null,
                request.locationId(), key, tenantContext.currentUser().subject()));
        idempotency.save(new IdempotencyRecord(organizationId, BATCH_CREATE, key, batch.getId()));
        return BatchResponse.from(batch);
    }

    @Transactional(readOnly = true)
    public BatchResponse get(UUID batchId) {
        UUID organizationId = requireMember();
        StockBatch batch = findBatch(batchId, organizationId);
        batch.refreshStatus(today());
        return BatchResponse.from(batch);
    }

    @Transactional
    public BatchResponse update(UUID batchId, BatchRequest request) {
        UUID organizationId = requireMember();
        StockBatch batch = findBatch(batchId, organizationId);
        validateReferences(organizationId, request.medicationId(), request.locationId());
        validateDateAndQuantity(request.expirationDate(), request.quantity());
        if (!batch.getLocationId().equals(request.locationId())) {
            throw new InventoryValidationException(
                    "Batch location changes must be recorded as transfer movements.");
        }
        String lotNumber = request.lotNumber().trim();
        if (batches.existsByOrganizationIdAndMedicationIdAndLotNumberAndIdNot(
                organizationId, request.medicationId(), lotNumber, batchId)) {
            throw new InventoryConflictException("Batch already exists for this medication.");
        }
        if (batch.getQuantity().compareTo(request.quantity()) != 0) {
            throw new InventoryValidationException(
                    "Batch quantity changes must be recorded as inventory movements.");
        }
        batch.update(request.locationId(), lotNumber, request.expirationDate(), request.quantity());
        batch.refreshStatus(today());
        return BatchResponse.from(batch);
    }

    @Transactional(readOnly = true)
    public BatchPageResponse search(UUID medicationId, UUID locationId, BatchStatus status,
            Pageable pageable) {
        UUID organizationId = requireMember();
        Page<StockBatch> result = batches.search(organizationId, medicationId, locationId, status,
                today(), pageable);
        result.forEach(batch -> batch.refreshStatus(today()));
        return BatchPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public BatchPageResponse expired(Pageable pageable) {
        UUID organizationId = requireMember();
        Page<StockBatch> result = batches.findAllByOrganizationIdAndExpirationDateBefore(
                organizationId, today(), pageable);
        result.forEach(batch -> batch.refreshStatus(today()));
        return BatchPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public BatchPageResponse expiring(int days, Pageable pageable) {
        if (days < 0 || days > 365) {
            throw new InventoryValidationException("Expiration window must be between 0 and 365 days.");
        }
        UUID organizationId = requireMember();
        LocalDate today = today();
        Page<StockBatch> result = batches.findAllByOrganizationIdAndExpirationDateBetween(
                organizationId, today, today.plusDays(days), pageable);
        result.forEach(batch -> batch.refreshStatus(today));
        return BatchPageResponse.from(result);
    }

    private void validateReferences(UUID organizationId, UUID medicationId, UUID locationId) {
        medications.findByIdAndOrganizationId(medicationId, organizationId)
                .orElseThrow(() -> new InventoryNotFoundException("Medication was not found."));
        locations.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new InventoryNotFoundException("Location was not found."));
    }

    private void validateDateAndQuantity(LocalDate expirationDate, BigDecimal quantity) {
        if (expirationDate.isBefore(today())) {
            throw new InventoryValidationException("Expiration date cannot be in the past.");
        }
        if (quantity.signum() <= 0) {
            throw new InventoryValidationException("Quantity must be greater than zero.");
        }
    }

    private StockBatch findBatch(UUID batchId, UUID organizationId) {
        return batches.findByIdAndOrganizationId(batchId, organizationId)
                .orElseThrow(() -> new InventoryNotFoundException("Batch was not found."));
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

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
