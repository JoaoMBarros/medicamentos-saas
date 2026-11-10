package br.com.medicacao.saas.catalog;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    boolean existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentation(
            UUID organizationId, String name, String activeIngredient,
            String concentration, String presentation);

    boolean existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentationAndIdNot(
            UUID organizationId, String name, String activeIngredient,
            String concentration, String presentation, UUID id);

    Optional<Medication> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
            select m from Medication m
            where m.organizationId = :organizationId
              and (:term is null
                   or lower(m.name) like lower(concat('%', :term, '%'))
                   or lower(m.activeIngredient) like lower(concat('%', :term, '%'))
                   or lower(m.presentation) like lower(concat('%', :term, '%')))
            """)
    Page<Medication> search(@Param("organizationId") UUID organizationId,
            @Param("term") String term, Pageable pageable);
}
