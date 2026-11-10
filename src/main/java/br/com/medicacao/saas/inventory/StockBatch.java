package br.com.medicacao.saas.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "stock_batches",
        uniqueConstraints = @UniqueConstraint(name = "uk_batch_organization_medication_lot",
                columnNames = {"organization_id", "medication_id", "lot_number"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class StockBatch {

    @Id
    @ToString.Include
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "medication_id", nullable = false)
    private UUID medicationId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "lot_number", nullable = false, length = 100)
    private String lotNumber;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.Version
    @Column(nullable = false)
    private long version;

    public StockBatch(UUID organizationId, UUID medicationId, UUID locationId,
            String lotNumber, LocalDate expirationDate, BigDecimal quantity) {
        this.organizationId = organizationId;
        this.medicationId = medicationId;
        this.locationId = locationId;
        this.lotNumber = lotNumber.trim();
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.status = BatchStatus.ACTIVE;
    }

    public void update(UUID locationId, String lotNumber, LocalDate expirationDate,
            BigDecimal quantity) {
        this.locationId = locationId;
        this.lotNumber = lotNumber.trim();
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }

    public void add(BigDecimal amount) {
        quantity = quantity.add(amount);
    }

    public void remove(BigDecimal amount) {
        quantity = quantity.subtract(amount);
    }

    public void adjust(BigDecimal amount) {
        quantity = quantity.add(amount);
    }

    public void transferTo(UUID targetLocationId) {
        locationId = targetLocationId;
    }

    public void refreshStatus(LocalDate today) {
        status = expirationDate.isBefore(today) ? BatchStatus.EXPIRED : BatchStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        StockBatch that = (StockBatch) other;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
