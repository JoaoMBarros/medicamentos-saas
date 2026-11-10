package br.com.medicacao.saas.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 80)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") String slug) {
}
