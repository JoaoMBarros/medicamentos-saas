package br.com.medicacao.saas.config;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.oidc")
public record OidcProperties(
        @NotBlank String jwkSetUri,
        @NotBlank String issuerUri,
        @NotBlank String audience) {
}
