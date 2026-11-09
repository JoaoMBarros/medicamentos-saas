---
name: create-feature
description: "Create a Spring Boot feature or REST endpoint in Java 21 with the required layers, validation, tests, and documentation."
argument-hint: "Describe the feature, endpoint, request, response, and business rules."
user-invocable: true
---

# Criar feature

## Procedimento

1. Identifique o contrato HTTP, regras de negócio, entradas, saídas e erros esperados.
2. Inspecione padrões existentes antes de criar pacotes ou classes.
3. Implemente DTOs, controller, service e persistência somente quando necessários.
4. Adicione validação de entrada e tratamento de erros consistente.
5. Crie testes para sucesso, validação e regras de negócio.
6. Atualize a documentação da API quando o contrato mudar.
7. Execute `gradle test` e reporte qualquer falha sem ocultá-la.

## Regras de commit

- Nenhum commit deve ser criado com coautoria de qualquer pessoa ou agente.
- Não adicione trailers `Co-authored-by` às mensagens de commit.
- Se um commit for solicitado, o autor deve ser exclusivamente o usuário.
- Nunca faça commits diretamente nas branches `develop` ou `master`.
- Sempre trabalhe e faça commits em uma branch `feature/*` ou `fix/*`.
- Se a branch atual for `develop`, `master` ou não seguir esses padrões, pare e
  solicite a criação ou troca para uma branch permitida antes de fazer commit.
