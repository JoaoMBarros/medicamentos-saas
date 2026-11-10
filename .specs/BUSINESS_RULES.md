# Regras de negócio

Este documento centraliza as regras funcionais e de consistência do sistema de
gestão de validade de medicamentos. As implementações devem seguir estas regras
e manter cobertura de testes para os comportamentos aplicáveis.

## 1. Organizações e isolamento de dados

- Uma organização é um tenant independente do sistema.
- Cada recurso pertencente a um tenant deve estar associado a uma
  `organization_id`.
- Usuários só podem consultar ou alterar recursos de organizações nas quais
  possuem associação ativa.
- Uma operação que tente acessar dados de outra organização deve ser rejeitada.
- Regras de autorização e isolamento devem ser aplicadas no servidor, nunca
  apenas pelo cliente.
- O identificador de um recurso pode ser igual ao de outro tenant sem criar
  conflito entre organizações.

## 2. Usuários, associações e permissões

- Uma associação de usuário é vinculada a exatamente uma organização.
- Uma combinação de organização e usuário não pode possuir mais de uma
  associação ativa.
- Os papéis de `OWNER` e `ADMIN` podem executar operações administrativas da
  organização.
- O proprietário da organização não pode ter seu papel alterado por uma
  operação comum.
- O proprietário da organização não pode ser removido por uma operação comum.
- Convites, alterações de papel, remoções e mudanças no ciclo de vida da
  organização devem gerar eventos de auditoria.

## 3. Medicamentos

- O cadastro de medicamentos pertence a uma organização.
- Uma organização não deve possuir medicamentos duplicados conforme os campos
  de identificação definidos pelo catálogo.
- O mesmo medicamento pode existir em organizações diferentes, pois cada
  organização mantém seu próprio catálogo e seus próprios dados.
- A criação e a alteração de um medicamento devem respeitar o tenant da
  operação.

## 4. Lotes e estoque

- Um lote pertence a uma organização e a um medicamento daquela mesma
  organização.
- Uma organização pode possuir vários lotes diferentes do mesmo medicamento.
- O mesmo número de lote pode existir em organizações diferentes sem conflito,
  pois os dados são isolados por tenant.
- Dentro da mesma organização, o mesmo número de lote não pode ser cadastrado
  duas vezes para o mesmo medicamento.
- Uma referência de lote para medicamento ou localização de outra organização
  deve ser rejeitada.
- Um lote deve possuir número de lote, data de validade, quantidade,
  medicamento e localização válidos.
- A quantidade de estoque não pode se tornar negativa, salvo se existir uma
  política explícita e autorizada para ajustes.
- Alterações de lote e o respectivo movimento de estoque devem ser persistidos
  na mesma transação.
- Movimentos de estoque são históricos e não devem ser apagados em operações
  normais.
- Atualizações concorrentes de estoque não podem sobrescrever alterações
  silenciosamente.

## 5. Idempotência

- Escritas que possam ser repetidas devem aceitar uma chave de idempotência
  quando definida pelo contrato do caso de uso.
- Repetir uma mesma operação idempotente não pode criar duplicidade de lote,
  movimento ou notificação.
- O processamento de itens de ingestão deve ser seguro para novas tentativas.

## 6. Ingestão de arquivos

- A ingestão é assíncrona e sempre pertence a uma organização.
- Uma linha inválida não deve invalidar silenciosamente outras linhas válidas.
- Erros de validação devem ser registrados por linha.
- Os estados permitidos para um job são `RECEIVED`, `PROCESSING`, `PARTIAL`,
  `COMPLETED` e `FAILED`.
- O reprocessamento de um job não deve duplicar lotes ou movimentos.

## 7. Validade e notificações

- Lotes próximos do vencimento devem ser identificados conforme a janela de
  alerta configurada.
- Lotes vencidos devem permanecer rastreáveis e não podem ser apagados como
  parte da operação normal.
- Uma notificação elegível deve ser deduplicada por uma chave de idempotência.
- Falhas transitórias de entrega devem ser repetidas com limite e backoff.
- Falhas permanentes devem ser isoladas para análise operacional.

## 8. Auditoria e segurança

- Alterações de permissões, associações, estoque, ingestão e notificações
  relevantes devem ser auditáveis.
- Logs e auditoria não devem expor tokens, segredos ou dados pessoais
  desnecessários.
- Endpoints protegidos exigem autenticação e autorização configuradas.

## 9. Regras ainda dependentes de definição

As regras abaixo exigem detalhamento antes da implementação dos respectivos
módulos:

- Quais campos definem a duplicidade de um medicamento no catálogo.
- Se um mesmo número de lote pode ser compartilhado por medicamentos diferentes
  dentro da mesma organização.
- A política autorizada para ajustes que poderiam resultar em quantidade
  negativa.
- As janelas de alerta de validade e as preferências por organização.
- O formato final das chaves de idempotência por operação.
