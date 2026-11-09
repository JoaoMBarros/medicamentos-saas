# medicacao-saas

Projeto inicial de uma API Spring Boot com Java 21 e Gradle.

## Requisitos

- Java 21
- Gradle 8.14 ou superior

## Executar

```bash
gradle bootRun
```

A API estará disponível em `http://localhost:8080`.

## Endpoints iniciais

- `GET /api/health`
- `GET /actuator/health`

## Testes

```bash
gradle test
```

O projeto usa JaCoCo para gerar o relatório de cobertura em
`build/reports/jacoco/test/html/index.html`. O `gradle check` falha quando a
cobertura global de linhas fica abaixo de 85%.

```bash
gradle check
```
