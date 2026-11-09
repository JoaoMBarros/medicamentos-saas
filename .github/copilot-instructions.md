# Diretrizes do projeto

## Stack e build

- Use Java 21 e Spring Boot conforme configurado em `build.gradle`.
- Use Gradle para compilar, testar e executar a aplicação.
- Preserve a configuração de toolchain Java 21; não introduza versões alternativas sem necessidade.
- Execute `gradle test` após alterações de código.

## Arquitetura

- Mantenha o código de produção em `br.com.medicacao.saas`.
- Separe responsabilidades por camadas e evite lógica de negócio em controllers.
- Prefira tipos explícitos, validação na borda da aplicação e tratamento de erros consistente.

## Qualidade

- Adicione ou atualize testes para cada comportamento novo ou alterado.
- Faça mudanças pequenas e compatíveis com o comportamento existente.
- Não adicione dependências sem justificar o uso e atualizar o build.
