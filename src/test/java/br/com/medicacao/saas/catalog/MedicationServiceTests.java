package br.com.medicacao.saas.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import br.com.medicacao.saas.identity.TenantAccessDeniedException;
import br.com.medicacao.saas.identity.TenantContext;
import br.com.medicacao.saas.organization.MembershipRepository;
import br.com.medicacao.saas.organization.MembershipRole;
import br.com.medicacao.saas.organization.MembershipStatus;
import br.com.medicacao.saas.organization.OrganizationMembership;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTests {

    @Mock MedicationRepository medications;
    @Mock MembershipRepository memberships;

    private final UUID organizationId = UUID.randomUUID();
    private MedicationService service;

    @BeforeEach
    void setUp() {
        service = new MedicationService(medications, memberships, new TenantContext());
        authenticate("user-1", organizationId.toString());
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsNormalizedMedicationForCurrentTenant() {
        MedicationRequest request = request("Tylenol", "Paracetamol", "500 mg", "Tablet");
        when(medications.existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentation(
                organizationId, "tylenol", "paracetamol", "500 mg", "tablet")).thenReturn(false);
        when(medications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MedicationResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("tylenol");
        verify(medications).save(argThat(m -> m.getOrganizationId().equals(organizationId)));
    }

    @Test
    void rejectsDuplicateMedication() {
        MedicationRequest request = request("Tylenol", "Paracetamol", "500 mg", "Tablet");
        when(medications.existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentation(
                organizationId, "tylenol", "paracetamol", "500 mg", "tablet")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(MedicationConflictException.class);
    }

    @Test
    void updatesAndSearchesOnlyCurrentTenant() {
        UUID medicationId = UUID.randomUUID();
        Medication medication = new Medication(organizationId, "Tylenol", "Paracetamol",
                "500 mg", "Tablet", null, null);
        when(medications.findByIdAndOrganizationId(medicationId, organizationId))
                .thenReturn(Optional.of(medication));
        when(medications
                .existsByOrganizationIdAndNameAndActiveIngredientAndConcentrationAndPresentationAndIdNot(
                        any(), any(), any(), any(), any(), any())).thenReturn(false);
        PageRequest page = PageRequest.of(0, 20);
        when(medications.search(organizationId, "para", page))
                .thenReturn(new PageImpl<>(java.util.List.of(medication), page, 1));

        assertThat(service.update(medicationId,
                request("Tylenol", "Paracetamol", "650 mg", "Tablet")).concentration())
                .isEqualTo("650 mg");
        assertThat(service.search(" para ", page).content()).hasSize(1);
        verify(medications).search(organizationId, "para", page);
    }

    @Test
    void rejectsInactiveMemberAndMissingMedication() {
        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.search(null, PageRequest.of(0, 20)))
                .isInstanceOf(TenantAccessDeniedException.class);

        when(memberships.findByOrganizationIdAndUserId(organizationId, "user-1"))
                .thenReturn(Optional.of(new OrganizationMembership(organizationId, "user-1",
                        "operator", MembershipRole.OPERATOR, MembershipStatus.ACTIVE)));
        when(medications.findByIdAndOrganizationId(any(), eq(organizationId)))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(UUID.randomUUID()))
                .isInstanceOf(MedicationNotFoundException.class);
    }

    private MedicationRequest request(String name, String ingredient, String concentration,
            String presentation) {
        return new MedicationRequest(name, ingredient, concentration, presentation, "12345", "78901234");
    }

    private void authenticate(String subject, String organization) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(subject)
                .claim("preferred_username", "operator").claim("organization_id", organization)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }
}
