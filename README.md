# Medicacao SaaS

Backend de um SaaS para gerenciamento de validade de medicamentos, com
suporte a múltiplas organizações, cadastro manual, ingestão em lote,
monitoramento de validade, auditoria e notificações.

## Visão geral

O projeto começa como um **modular monolith** para reduzir a complexidade
operacional e permitir evolução rápida. Os módulos devem manter limites claros:

- Identity: autenticação e autorização
- Organization: tenants, usuários e permissões
- Catalog: medicamentos e normalização
- Inventory: lotes, locais e movimentações
- Ingestion: importação de arquivos e processamento assíncrono
- Expiration: regras de validade
- Notification: alertas e entregas
- Audit: rastreabilidade das operações

Ingestões grandes e notificações são processadas de forma assíncrona. A
extração para microsserviços só deve ocorrer quando métricas de escala,
isolamento ou ownership justificarem a complexidade.

Mais detalhes estão em:

- [Architecture](.specs/ARCHITECTURE.md)
- [Stack](.specs/STACK.md)
- [Roadmap](.specs/ROADMAP.md)
- [Tasks](.specs/TASKS.md)

## Estrutura de módulos

Os módulos são representados por pacotes sob
`br.com.medicacao.saas` e devem evoluir com dependências direcionadas:

```text
br.com.medicacao.saas
├── identity       # autenticação e autorização
├── organization  # tenants, usuários e permissões
├── catalog       # medicamentos e normalização
├── inventory     # lotes, locais e movimentações
├── ingestion     # ingestão e validação de dados
├── expiration    # regras e consultas de validade
├── notification  # notificações e entregas
└── audit         # rastreabilidade operacional
```

Controllers pertencem à camada de entrada e devem delegar para casos de uso
dos módulos. Repositories, adapters e integrações externas não devem ser
acessados diretamente por controllers. A criação de novos módulos ou
dependências entre módulos deve preservar esses limites.

## Stack

- Java 21
- Spring Boot
- Gradle
- Spring MVC
- Spring Security com OAuth 2.0/OIDC
- PostgreSQL como banco transacional
- Spring Data JPA/Hibernate
- Flyway para migrations
- RabbitMQ para processamento assíncrono
- Redis opcional para cache
- S3-compatible storage para arquivos de ingestão
- Actuator, Micrometer e OpenTelemetry
- JUnit 5, Spring Boot Test, MockMvc e Testcontainers
- JaCoCo com cobertura mínima global de 85%

## Requisitos

- Java 21
- Gradle 8.14 ou superior, ou o Gradle Wrapper incluído no projeto

Verifique a instalação:

```bash
java -version
./gradlew --version
```

No Windows:

```powershell
java -version
.\gradlew.bat --version
```

Atualmente não há variáveis de ambiente obrigatórias para executar o endpoint
inicial de saúde. Integrações futuras devem documentar suas variáveis sem
incluir valores sensíveis no repositório.

## Executar

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

No Windows:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

A API estará disponível em `http://localhost:8080`.

Para executar a infraestrutura local, defina `POSTGRES_PASSWORD` sem
versioná-la e inicie o PostgreSQL:

```bash
export POSTGRES_PASSWORD='<local-password>'
docker compose up -d postgres
./gradlew bootRun --args='--spring.profiles.active=local'
```

No PowerShell:

```powershell
$env:POSTGRES_PASSWORD = '<local-password>'
docker compose up -d postgres
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## Endpoints iniciais

- `GET /api/health`
- `GET /actuator/health`

Endpoints de negócio devem exigir autenticação. Health checks operacionais
devem ser expostos somente conforme a configuração da infraestrutura.

## Testes e cobertura

Execute os testes:

```bash
./gradlew test
```

Execute a validação completa:

```bash
./gradlew check
```

No Windows, substitua `./gradlew` por `.\gradlew.bat`.

O JaCoCo gera o relatório HTML em
`build/reports/jacoco/test/html/index.html`. O `check` falha quando a cobertura
global de linhas fica abaixo de 85%.

## Configuração

Use arquivos YAML para valores não sensíveis que podem mudar sem novo deploy de
código, como janelas de alerta, limites de paginação, nomes de filas e feature
flags.

Segredos, tokens, credenciais, chaves privadas e dados sensíveis nunca devem
ser armazenados em arquivos versionados, incluídos no commit ou enviados ao
repositório. Use variáveis de ambiente ou um gerenciador de segredos.

Para desenvolvimento local, use o arquivo de exemplo como referência e
substitua os valores locais. O Docker Compose carrega automaticamente um
arquivo `.env`; o Spring Boot deve receber essas variáveis pela shell, IDE ou
gerenciador de secrets:

```bash
cp .env.example .env
```

No PowerShell:

```powershell
Copy-Item .env.example .env
```

Depois, carregue as variáveis no ambiente antes de executar a aplicação. O
arquivo `.env` é ignorado pelo Git e nunca deve ser commitado.

## Segurança e multi-tenancy

- Todos os serviços e endpoints externos devem exigir autenticação.
- A API valida tokens JWT por OAuth 2.0 Resource Server; configure
  `OIDC_JWK_SET_URI` para o endpoint de chaves do provedor de identidade.
- Toda operação deve validar autorização e vínculo com a organização.
- Entidades pertencentes a tenants devem possuir `organization_id`.
- O histórico de movimentações deve ser auditável e não deve ser apagado em
  operações normais.
- Logs e mensagens de erro não devem expor tokens, credenciais ou dados pessoais
  desnecessários.

## Fluxos principais

### Cadastro manual

O usuário envia medicamento, lote, quantidade, local e validade. A API valida a
entrada, atualiza o estoque, registra a movimentação na mesma transação e
retorna um contrato imutável.

### Ingestão

O cliente cria um job, envia o arquivo para object storage e acompanha o
processamento assíncrono. O worker valida cada linha, permite sucesso parcial,
registra erros por item e usa idempotência para evitar duplicidades.

### Alertas

Um job agendado identifica lotes próximos do vencimento, cria notificações
deduplicadas e delega a entrega para um worker com retry e dead-letter.
