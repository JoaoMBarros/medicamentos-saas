---
name: Java e Spring Boot
description: "Use when creating or modifying Java 21, Spring Boot, REST controllers, services, configuration, dependency injection, or application layers."
applyTo: "src/**/*.java"
---

# Java 21 e Spring Boot

- Use recursos compatíveis com Java 21 e mantenha classes pequenas e coesas.
- Prefira injeção por construtor; não use campos com `@Autowired`.
- Controllers devem coordenar HTTP e delegar regras de negócio para services.
- Use DTOs para contratos externos; não exponha entidades de persistência diretamente.
- Valide entradas com Bean Validation e retorne respostas HTTP coerentes.
- Preserve o pacote raiz `br.com.medicacao.saas` para que o component scan funcione.
