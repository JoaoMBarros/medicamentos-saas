package br.com.medicacao.saas.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.medicacao.saas.catalog.Location;
import br.com.medicacao.saas.catalog.LocationRepository;
import br.com.medicacao.saas.catalog.LocationType;
import br.com.medicacao.saas.catalog.Medication;
import br.com.medicacao.saas.catalog.MedicationRepository;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipRole;
import br.com.medicacao.saas.organization.MembershipStatus;
import br.com.medicacao.saas.organization.OrganizationMembership;

@ExtendWith(MockitoExtension.class)
class StockBatchServiceTests {

    @Mock StockBatchRepository batches;
    @Mock InventoryMovementRepository movements;
    @Mock IdempotencyRecordRepository idempotency;
    @Mock MedicationRepository medications;
    @Mock LocationRepository locations;
    @Mock MembershipRepository memberships;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID medicationId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-10T00:00:00Z"),
            ZoneOffset.UTC);
    private StockBatchService service;

    @BeforeEach
    void setUp() {
        service = new StockBatchService(batches, movements, idempotency, medications, locations,
                memberships, new TenantContext(), clock);
        authenticate();
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
        lenient().when(medications.findByIdAndOrganizationId(medicationId, organizationId))
                .thenReturn(Optional.of(mock(Medication.class)));
        lenient().when(locations.findByIdAndOrganizationId(locationId, organizationId))
                .thenReturn(Optional.of(mock(Location.class)));
        lenient().when(batches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(idempotency.findByOrganizationIdAndOperationAndIdempotencyKey(
                any(), any(), any())).thenReturn(Optional.empty());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsBatchAndInitialMovementAtomically() {
        BatchResponse response = service.create(request("LOT-1", "5"), "batch-key");

        assertThat(response.quantity()).isEqualByComparingTo("5");
        assertThat(response.status()).isEqualTo(BatchStatus.ACTIVE);
        verify(movements).save(argThat(m -> m.getType() == MovementType.ADDITION
                && m.getQuantity().compareTo(new BigDecimal("5")) == 0));
        verify(idempotency).save(any(IdempotencyRecord.class));
    }

    @Test
    void rejectsDuplicateBatchAndPastExpiration() {
        BatchRequest request = request("LOT-1", "5");
        when(batches.existsByOrganizationIdAndMedicationIdAndLotNumber(
                organizationId, medicationId, "LOT-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request, "key-1"))
                .isInstanceOf(InventoryConflictException.class);

        assertThatThrownBy(() -> service.create(new BatchRequest(medicationId, locationId,
                "LOT-2", LocalDate.of(2026, 11, 9), new BigDecimal("5")), "key-2"))
                .isInstanceOf(InventoryValidationException.class);
    }

    @Test
    void preventsQuantityMutationOutsideMovementAndSupportsExpiryQueries() {
        StockBatch batch = new StockBatch(organizationId, medicationId, locationId, "LOT-1",
                LocalDate.of(2026, 11, 20), new BigDecimal("5"));
        UUID batchId = UUID.randomUUID();
        when(batches.findByIdAndOrganizationId(batchId, organizationId))
                .thenReturn(Optional.of(batch));
        when(batches.existsByOrganizationIdAndMedicationIdAndLotNumberAndIdNot(
                any(), any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.update(batchId, request("LOT-1", "6")))
                .isInstanceOf(InventoryValidationException.class);

        PageRequest page = PageRequest.of(0, 20);
        when(batches.findAllByOrganizationIdAndExpirationDateBefore(
                organizationId, LocalDate.of(2026, 11, 10), page))
                .thenReturn(new PageImpl<>(java.util.List.of(batch), page, 1));
        assertThat(service.expired(page).content()).hasSize(1);
    }

    @Test
    void replaysIdempotentBatchAndRejectsInvalidExpirationWindow() {
        UUID existingBatchId = UUID.randomUUID();
        when(idempotency.findByOrganizationIdAndOperationAndIdempotencyKey(
                organizationId, "BATCH_CREATE", "same-key"))
                .thenReturn(Optional.of(new IdempotencyRecord(organizationId, "BATCH_CREATE",
                        "same-key", existingBatchId)));
        StockBatch existingBatch = new StockBatch(organizationId, medicationId, locationId,
                "LOT-1", LocalDate.of(2026, 11, 20), new BigDecimal("5"));
        when(batches.findByIdAndOrganizationId(existingBatchId, organizationId))
                .thenReturn(Optional.of(existingBatch));

        assertThat(service.create(request("LOT-1", "5"), "same-key").lotNumber())
                .isEqualTo("LOT-1");
        assertThatThrownBy(() -> service.expiring(366, PageRequest.of(0, 20)))
                .isInstanceOf(InventoryValidationException.class);
    }

    private BatchRequest request(String lot, String quantity) {
        return new BatchRequest(medicationId, locationId, lot,
                LocalDate.of(2026, 11, 20), new BigDecimal(quantity));
    }

    private void authenticate() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("user-1")
                .claim("preferred_username", "operator")
                .claim("organization_id", organizationId.toString())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
