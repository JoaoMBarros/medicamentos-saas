---
name: Arquitetura
description: "Use when designing features, packages, REST APIs, services, repositories, DTOs, or cross-layer changes in this Spring Boot project."
applyTo: "src/**/*.java"
---

# Arquitetura

- Organize novas funcionalidades por domínio, mantendo dependências entre camadas direcionadas para dentro.
- Mantenha controllers sem regras de negócio e repositories sem decisões de aplicação.
- Use interfaces apenas quando houver uma necessidade real de abstração ou substituição.
- Defina contratos de request/response explicitamente e evite vazar detalhes internos.
- Considere impacto em configuração, observabilidade, testes e documentação ao alterar uma API.
