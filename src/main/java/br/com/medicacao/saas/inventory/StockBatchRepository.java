package br.com.medicacao.saas.inventory;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockBatchRepository extends JpaRepository<StockBatch, UUID> {

    Optional<StockBatch> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndMedicationIdAndLotNumber(
            UUID organizationId, UUID medicationId, String lotNumber);

    boolean existsByOrganizationIdAndMedicationIdAndLotNumberAndIdNot(
            UUID organizationId, UUID medicationId, String lotNumber, UUID id);

    @Query("""
            select b from StockBatch b
            where b.organizationId = :organizationId
              and (:medicationId is null or b.medicationId = :medicationId)
              and (:locationId is null or b.locationId = :locationId)
              and (:status is null
                   or (:status = br.com.medicacao.saas.inventory.BatchStatus.ACTIVE
                       and b.expirationDate >= :today)
                   or (:status = br.com.medicacao.saas.inventory.BatchStatus.EXPIRED
                       and b.expirationDate < :today))
            """)
    Page<StockBatch> search(@Param("organizationId") UUID organizationId,
            @Param("medicationId") UUID medicationId, @Param("locationId") UUID locationId,
            @Param("status") BatchStatus status, @Param("today") LocalDate today,
            Pageable pageable);

    Page<StockBatch> findAllByOrganizationIdAndExpirationDateBefore(
            UUID organizationId, LocalDate date, Pageable pageable);

    Page<StockBatch> findAllByOrganizationIdAndExpirationDateBetween(
            UUID organizationId, LocalDate from, LocalDate to, Pageable pageable);
}
