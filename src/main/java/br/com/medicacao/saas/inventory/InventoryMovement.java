package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "inventory_movements",
        uniqueConstraints = @UniqueConstraint(name = "uk_movement_idempotency",
                columnNames = {"organization_id", "operation", "idempotency_key"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class InventoryMovement {

    @Id
    @ToString.Include
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Column(name = "from_location_id")
    private UUID fromLocationId;

    @Column(name = "to_location_id")
    private UUID toLocationId;

    @Column(name = "idempotency_key", nullable = false, length = 200)
    private String idempotencyKey;

    @Column(name = "actor_user_id", nullable = false, length = 255)
    private String actorUserId;

    @Column(nullable = false)
    private Instant createdAt;

    public InventoryMovement(UUID organizationId, UUID batchId, MovementType type,
            BigDecimal quantity, UUID fromLocationId, UUID toLocationId,
            String idempotencyKey, String actorUserId) {
        this.organizationId = organizationId;
        this.batchId = batchId;
        this.type = type;
        this.quantity = quantity;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.idempotencyKey = idempotencyKey;
        this.actorUserId = actorUserId;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        InventoryMovement that = (InventoryMovement) other;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
