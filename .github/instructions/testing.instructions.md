---
name: Testes
description: "Use when adding, modifying, or reviewing unit tests, integration tests, Spring Boot tests, or test validation for Java code."
applyTo: ["src/test/**/*.java", "src/main/**/*.java"]
---

# Testes

- Teste comportamento observável, não detalhes internos de implementação.
- Use testes unitários para regras isoladas e `@SpringBootTest` apenas quando o contexto Spring for necessário.
- Cubra cenários válidos, inválidos e limites relevantes.
- Mantenha pelo menos 85% de cobertura global de linhas; o JaCoCo deve bloquear o build abaixo desse limite.
- Mantenha testes determinísticos, independentes e sem dependência de serviços externos.
- Execute `gradle check` para validar testes e cobertura antes de concluir a alteração.
