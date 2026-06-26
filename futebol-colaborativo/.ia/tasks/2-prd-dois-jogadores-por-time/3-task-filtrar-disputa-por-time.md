# Task 3: Filtrar Disputa por Time no SistemaFutebol

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`

## Objetivo

Impedir que `localizarOponenteProximoParaDisputa` retorne jogadores do mesmo time,
garantindo que aliados nunca iniciem disputa entre si.

## Escopo

Alterar apenas `SistemaFutebol.java`.

## Requisitos

Em `localizarOponenteProximoParaDisputa`, antes de considerar um candidato como oponente,
verificar se ele pertence a um time diferente usando o mapa `configuracoes` que ja existe:

```java
ConfiguracaoJogador confAtual = configuracoes.get(nome);
ConfiguracaoJogador confCandidato = configuracoes.get(nomeOponente);

if (confAtual == null || confCandidato == null) continue;
if (confAtual.getTime() == confCandidato.getTime()) continue; // aliado, ignorar
```

A verificacao deve ser feita no inicio do loop, antes de qualquer outro criterio.

## Fora de Escopo

- Alterar `JogadorAgent`.
- Alterar `JogadorEstado`.
- Alterar o payload do WebSocket.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

Validacao manual com `mvn exec:java`: verificar nos logs do terminal que nenhum CFP
de disputa e enviado de `azul-*` para `azul-*` nem de `vermelho-*` para `vermelho-*`.

## Criterios de Sucesso

- Codigo compila.
- Testes passam.
- Nenhuma disputa entre aliados ocorre na simulacao.
- Disputa entre adversarios (`azul-*` vs `vermelho-*`) continua ocorrendo normalmente.
