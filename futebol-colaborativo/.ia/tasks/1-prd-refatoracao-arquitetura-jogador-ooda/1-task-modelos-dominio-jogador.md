# Task 3: Criar Modelos de Dominio do Jogador

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`

## Objetivo

Criar os tipos basicos de dominio que serao usados pela refatoracao OODA do `JogadorAgent`.

## Escopo

Criar no pacote `com.futebol.colaborativo.jogo`:

- `TipoDecisao`
- `PapelJogador`
- `Time`
- `ConfiguracaoJogador`

## Requisitos

`TipoDecisao` deve conter:

```text
PERSEGUIR_BOLA
AGIR_COM_BOLA
INTERCEPTAR
MANTER_POSICAO_DEFENSIVA
MANTER_POSICAO
```

`PapelJogador` deve conter:

```text
JOGADOR
ATACANTE
ZAGUEIRO
```

`Time` deve conter:

```text
AZUL
VERMELHO
```

`ConfiguracaoJogador` deve guardar:

```text
nome
xInicial
yInicial
golX
time
papel
```

## Fora de Escopo

- Alterar `JogadorAgent`.
- Alterar criacao dos jogadores no `App`.
- Criar atacante/zagueiro como subclasses.
- Criar 2 jogadores por time.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Os novos tipos compilam.
- Nenhum comportamento da simulacao muda.
- Nenhum payload do frontend muda.

## Resultado

- Criados os modelos de dominio em `com.futebol.colaborativo.jogo`.
- Validacao executada com sucesso: `mvn test`.
