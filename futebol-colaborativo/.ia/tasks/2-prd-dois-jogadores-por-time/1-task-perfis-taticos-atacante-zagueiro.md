# Task 1: Criar Perfis Taticos de Atacante e Zagueiro

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`

## Objetivo

Adicionar `PerfilTatico.atacante()` e `PerfilTatico.zagueiro()` com pesos distintos
para o caso de bola livre, onde a diferenca de comportamento entre papeis e expressada.

## Escopo

Alterar apenas `PerfilTatico.java` e seus testes.

## Requisitos

Adicionar ao lado de `equilibrado()`:

```java
public static PerfilTatico atacante() { ... }
public static PerfilTatico zagueiro() { ... }
```

Pesos para `atacante()`:

```text
Bola livre:
  PERSEGUIR_BOLA = 75
  MANTER_POSICAO_DEFENSIVA = 25

Outro jogador com bola:
  INTERCEPTAR = 100

Eu com bola:
  AGIR_COM_BOLA = 100
```

Pesos para `zagueiro()`:

```text
Bola livre:
  PERSEGUIR_BOLA = 25
  MANTER_POSICAO_DEFENSIVA = 75

Outro jogador com bola:
  INTERCEPTAR = 100

Eu com bola:
  AGIR_COM_BOLA = 100
```

## Testes unitarios a adicionar

Em `PerfilTaticoTest`:

- `atacante()` tem peso de `PERSEGUIR_BOLA` maior que `zagueiro()` no mapa de bola livre.
- `zagueiro()` tem peso de `MANTER_POSICAO_DEFENSIVA` maior que `atacante()` no mapa de bola livre.
- Os mapas de `outroJogadorComBola` e `comBola` sao identicos entre `atacante()` e `zagueiro()`.

## Fora de Escopo

- Alterar `JogadorAgent` para usar os novos perfis (isso e feito na Task 2).
- Alterar qualquer outro arquivo.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Classes compilam.
- Testes novos passam.
- Testes existentes continuam passando.
