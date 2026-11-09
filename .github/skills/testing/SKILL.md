---
name: testing
description: "Create, improve, or run tests for Java 21 and Spring Boot applications, including unit, integration, controller, validation, and context tests."
argument-hint: "Describe the class, behavior, or failing test to verify."
user-invocable: true
---

# Testes

## Procedimento

1. Identifique o comportamento que precisa ser protegido.
2. Escolha teste unitário ou integração conforme a dependência real do Spring.
3. Cubra cenário nominal, entradas inválidas e limites relevantes.
4. Execute o teste mais específico e depois `gradle test`.
5. Analise falhas pela causa raiz e não transforme o teste em um falso positivo.
