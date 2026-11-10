package br.com.medicacao.saas.organization;

import jakarta.validation.constraints.NotNull;

public record RoleRequest(@NotNull MembershipRole role) {
}
