package br.com.medicacao.saas.catalog;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    boolean existsByOrganizationIdAndNameAndType(UUID organizationId, String name, LocationType type);

    boolean existsByOrganizationIdAndNameAndTypeAndIdNot(UUID organizationId, String name,
            LocationType type, UUID id);

    Optional<Location> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Query("""
            select l from Location l
            where l.organizationId = :organizationId
              and (:term is null or lower(l.name) like lower(concat('%', :term, '%')))
            """)
    Page<Location> search(@Param("organizationId") UUID organizationId,
            @Param("term") String term, Pageable pageable);
}
