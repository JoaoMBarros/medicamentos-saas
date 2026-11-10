package br.com.medicacao.saas.catalog;

import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipStatus;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medications;
    private final MembershipRepository memberships;
    private final TenantContext tenantContext;

    @Transactional
    public MedicationResponse create(MedicationRequest request) {
        UUID organizationId = requireMember();
        if (isDuplicate(organizationId, request)) {
            throw new MedicationConflictException("Medication already exists in this organization.");
        }
        return MedicationResponse.from(medications.save(new Medication(organizationId,
                request.name(), request.activeIngredient(), request.concentration(),
                request.presentation(), request.anvisaRegistration(), request.ean())));
    }

    @Transactional(readOnly = true)
    public MedicationResponse get(UUID medicationId) {
        UUID organizationId = requireMember();
        return MedicationResponse.from(findMedication(medicationId, organizationId));
    }

    @Transactional
    public MedicationResponse update(UUID medicationId, MedicationRequest request) {
        UUID organizationId = requireMember();
        Medication medication = findMedication(medicationId, organizationId);
        if (isDuplicate(organizationId, request, medicationId)) {
            throw new MedicationConflictException("Medication already exists in this organization.");
        }
        medication.update(request.name(), request.activeIngredient(), request.concentration(),
                request.presentation(), request.anvisaRegistration(), request.ean());
        return MedicationResponse.from(medication);
    }

    @Transactional(readOnly = true)
    public MedicationPageResponse search(String term, Pageable pageable) {
        UUID organizationId = requireMember();
        String normalizedTerm = term == null || term.isBlank()
                ? null
                : term.trim().toLowerCase(Locale.ROOT);
        Page<Medication> result = medications.search(organizationId, normalizedTerm, pageable);
        return MedicationPageResponse.from(result);
    }

    private Medication findMedication(UUID medicationId, UUID organizationId) {
        return medications.findByIdAndOrganizationId(medicationId, organizationId)
                .orElseThrow(() -> new MedicationNotFoundException("Medication was not found."));
    }

    private boolean isDuplicate(UUID organizationId, MedicationRequest request) {
        return medications.existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentation(
                organizationId, normalize(request.name()), normalize(request.activeIngredient()),
                normalize(request.concentration()), normalize(request.presentation()));
    }

    private boolean isDuplicate(UUID organizationId, MedicationRequest request, UUID medicationId) {
        return medications
                .existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentationAndIdNot(
                        organizationId, normalize(request.name()), normalize(request.activeIngredient()),
                        normalize(request.concentration()), normalize(request.presentation()), medicationId);
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

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
