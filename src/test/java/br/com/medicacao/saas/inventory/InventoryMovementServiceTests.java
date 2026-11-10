package br.com.medicacao.saas.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import br.com.medicacao.saas.catalog.Location;
import br.com.medicacao.saas.catalog.LocationRepository;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipRole;
import br.com.medicacao.saas.organization.MembershipStatus;
import br.com.medicacao.saas.organization.OrganizationMembership;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTests {

    @Mock StockBatchRepository batches;
    @Mock InventoryMovementRepository movements;
    @Mock IdempotencyRecordRepository idempotency;
    @Mock LocationRepository locations;
    @Mock MembershipRepository memberships;

    private final UUID organizationId = UUID.randomUUID();
    private final UUID batchId = UUID.randomUUID();
    private final UUID currentLocationId = UUID.randomUUID();
    private final UUID targetLocationId = UUID.randomUUID();
    private InventoryMovementService service;

    @BeforeEach
    void setUp() {
        service = new InventoryMovementService(batches, movements, idempotency, locations,
                memberships, new TenantContext());
        authenticate();
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
        lenient().when(movements.findByOrganizationIdAndTypeAndIdempotencyKey(
                any(), any(), any())).thenReturn(Optional.empty());
        lenient().when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(batches.findByIdAndOrganizationId(batchId, organizationId))
                .thenReturn(Optional.of(batch(new BigDecimal("5"))));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void removesStockAndRejectsNegativeResult() {
        StockBatch batch = batch(new BigDecimal("5"));
        when(batches.findByIdAndOrganizationId(batchId, organizationId))
                .thenReturn(Optional.of(batch));

        MovementResponse response = service.create(batchId,
                new MovementRequest(MovementType.REMOVAL, new BigDecimal("2"), null), "move-1");

        assertThat(response.type()).isEqualTo(MovementType.REMOVAL);
        assertThat(batch.getQuantity()).isEqualByComparingTo("3");
        assertThatThrownBy(() -> service.create(batchId,
                new MovementRequest(MovementType.REMOVAL, new BigDecimal("4"), null), "move-2"))
                .isInstanceOf(InventoryValidationException.class);
    }

    @Test
    void transfersOnlyToAnotherTenantLocationAndKeepsHistory() {
        StockBatch batch = batch(new BigDecimal("5"));
        when(batches.findByIdAndOrganizationId(batchId, organizationId))
                .thenReturn(Optional.of(batch));
        when(locations.findByIdAndOrganizationId(targetLocationId, organizationId))
                .thenReturn(Optional.of(mock(Location.class)));

        service.create(batchId, new MovementRequest(MovementType.TRANSFER,
                new BigDecimal("5"), targetLocationId), "move-transfer");

        assertThat(batch.getLocationId()).isEqualTo(targetLocationId);
        verify(movements).save(argThat(m -> m.getFromLocationId().equals(currentLocationId)
                && m.getToLocationId().equals(targetLocationId)));

        UUID otherLocationId = UUID.randomUUID();
        when(locations.findByIdAndOrganizationId(otherLocationId, organizationId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(batchId, new MovementRequest(MovementType.TRANSFER,
                new BigDecimal("1"), otherLocationId), "move-invalid"))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void requiresIdempotencyKey() {
        assertThatThrownBy(() -> service.create(batchId,
                new MovementRequest(MovementType.ADDITION, new BigDecimal("1"), null), " "))
                .isInstanceOf(InventoryValidationException.class);
    }

    private StockBatch batch(BigDecimal quantity) {
        return new StockBatch(organizationId, UUID.randomUUID(), currentLocationId, "LOT-1",
                LocalDate.of(2026, 12, 1), quantity);
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
