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

## Autenticação

- Use um provedor OIDC externo, como Keycloak, como Authorization Server.
- Para clientes com usuários, use OAuth 2.0 Authorization Code com PKCE.
- Mantenha o Spring Boot como Resource Server, validando assinatura, issuer,
  audience e expiração dos access tokens.
- Não implemente login, emissão de tokens ou armazenamento de senhas no
  monólito sem uma decisão explícita de arquitetura.
- Password Grant só pode ser usado temporariamente em ambiente local ou de
  testes; nunca o trate como fluxo de produção.

## Qualidade

- Adicione ou atualize testes para cada comportamento novo ou alterado.
- Faça mudanças pequenas e compatíveis com o comportamento existente.
- Não adicione dependências sem justificar o uso e atualizar o build.
- Nunca versione, faça commit ou push de dados sensíveis, segredos, credenciais,
  tokens, chaves privadas ou dados pessoais desnecessários.
- Revise os arquivos staged antes de qualquer commit e remova dados sensíveis.
