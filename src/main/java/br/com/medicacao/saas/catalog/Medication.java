package br.com.medicacao.saas.catalog;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "medications",
        uniqueConstraints = @UniqueConstraint(name = "uk_medication_catalog_identity",
                columnNames = {"organization_id", "name", "active_ingredient",
                        "concentration", "presentation"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(onlyExplicitlyIncluded = true)
public class Medication {

    @Id
    @ToString.Include
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "active_ingredient", nullable = false, length = 160)
    private String activeIngredient;

    @Column(nullable = false, length = 80)
    private String concentration;

    @Column(nullable = false, length = 120)
    private String presentation;

    @Column(name = "anvisa_registration", length = 40)
    private String anvisaRegistration;

    @Column(length = 14)
    private String ean;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @jakarta.persistence.Version
    @Column(nullable = false)
    private long version;

    public Medication(UUID organizationId, String name, String activeIngredient,
            String concentration, String presentation, String anvisaRegistration, String ean) {
        this.organizationId = organizationId;
        this.name = normalizeRequired(name);
        this.activeIngredient = normalizeRequired(activeIngredient);
        this.concentration = normalizeRequired(concentration);
        this.presentation = normalizeRequired(presentation);
        this.anvisaRegistration = normalizeOptional(anvisaRegistration);
        this.ean = normalizeOptional(ean);
    }

    public void update(String name, String activeIngredient, String concentration,
            String presentation, String anvisaRegistration, String ean) {
        this.name = normalizeRequired(name);
        this.activeIngredient = normalizeRequired(activeIngredient);
        this.concentration = normalizeRequired(concentration);
        this.presentation = normalizeRequired(presentation);
        this.anvisaRegistration = normalizeOptional(anvisaRegistration);
        this.ean = normalizeOptional(ean);
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

    private static String normalizeRequired(String value) {
        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Medication that = (Medication) other;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
