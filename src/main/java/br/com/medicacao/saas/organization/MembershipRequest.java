package br.com.medicacao.saas.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MembershipRequest(
        @NotBlank @Size(max = 255) String userId,
        @NotBlank @Size(max = 255) String username,
        @NotNull MembershipRole role) {
}
