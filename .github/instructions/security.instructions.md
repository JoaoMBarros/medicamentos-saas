---
name: Segurança
description: "Use when handling authentication, authorization, user input, secrets, HTTP endpoints, logging, dependencies, or security-sensitive Java and Spring Boot changes."
applyTo: ["src/**/*.java", "src/main/resources/**/*.yml", "src/main/resources/**/*.properties"]
---

# Segurança

- Nunca registre, versione ou exponha segredos, tokens, credenciais ou dados pessoais desnecessários.
- Valide e normalize entradas externas antes de usá-las.
- Aplique autorização no servidor; não confie em validações feitas pelo cliente.
- Evite mensagens de erro que revelem detalhes internos ou dados sensíveis.
- Revise dependências novas e prefira versões gerenciadas pelo Spring Boot.
- Use Keycloak ou outro provedor OIDC externo como Authorization Server.
- Para autenticação de usuários, prefira Authorization Code com PKCE e clients
  públicos sem `client_secret` no frontend.
- O backend deve permanecer como Resource Server e validar issuer, audience,
  assinatura e expiração dos access tokens.
- Restrinja Password Grant a desenvolvimento/testes locais e não o habilite
  como solução de produção.
