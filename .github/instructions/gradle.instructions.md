---
name: Gradle
description: "Use when changing Gradle builds, dependencies, plugins, tasks, Java toolchains, or project commands."
applyTo: ["build.gradle", "settings.gradle", "gradle.properties", "gradle/**/*.gradle"]
---

# Gradle

- Preserve Java 21 no toolchain e use dependências gerenciadas pelo Spring Boot.
- Prefira configurações e tarefas existentes antes de criar lógica customizada.
- Use repositórios confiáveis e evite dependências duplicadas ou sem manutenção.
- Após alterações no build, execute `gradle test` ou a tarefa mais específica disponível.
- Documente no README comandos novos ou mudanças no fluxo de execução.
