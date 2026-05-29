# Task 9: Consolidar Logica do Tick em decidirAcaoPrincipal

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Mover os tres `if`s de pre-condicao do `onTick()` para dentro de `decidirAcaoPrincipal()`,
deixando o ticker com uma unica chamada e toda a logica do agente em um so lugar.

## Escopo

Alterar apenas `JogadorAgent.java`:

- `onTick()` passa a ter apenas `decidirAcaoPrincipal()`.
- `decidirAcaoPrincipal()` absorve as verificacoes de penalidade, disputa em andamento e tentativa de disputa, antes do fluxo OODA.

## Fora de Escopo

- Alterar qualquer logica de jogo.
- Renomear metodos.
- Alterar qualquer outro arquivo.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Codigo compila.
- Testes passam.
- Nenhum comportamento da simulacao muda.

## Resultado

- `onTick()` passou a ter apenas `decidirAcaoPrincipal()`.
- `decidirAcaoPrincipal()` absorveu as verificacoes de penalidade, disputa em andamento e tentativa de disputa antes do fluxo OODA.
- `atualizarEstado()` movido para dentro de `decidirAcaoPrincipal()`, no caminho OODA e no caminho de tentativa de disputa.
