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
