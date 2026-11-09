---
name: code-review
description: "Review Java 21, Spring Boot, and Gradle changes for correctness, regressions, maintainability, testing gaps, and security issues."
argument-hint: "Describe the files, branch, or change set to review."
user-invocable: true
---

# Revisão de código

## Procedimento

1. Entenda o objetivo da mudança e compare com o comportamento atual.
2. Verifique fluxo de dados, contratos HTTP, validação, tratamento de erros e compatibilidade.
3. Procure regressões, falhas de segurança, problemas de concorrência e cobertura insuficiente.
4. Confirme que o build e os testes relevantes foram executados.
5. Relate primeiro problemas acionáveis, ordenados por severidade, com arquivo e linha.
6. Diferencie problemas comprovados de dúvidas ou sugestões de melhoria.
