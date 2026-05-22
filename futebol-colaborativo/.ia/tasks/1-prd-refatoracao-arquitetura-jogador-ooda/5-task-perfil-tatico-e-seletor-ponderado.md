# Task 5: Criar PerfilTatico e SeletorDecisaoPonderada

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Criar a base de decisao probabilistica minima para o ciclo OODA.

## Escopo

Criar no pacote `com.futebol.colaborativo.estrategia`:

- `PerfilTatico`
- `SeletorDecisaoPonderada`

## Requisitos

`PerfilTatico.equilibrado()` deve representar:

```text
Bola livre:
  PERSEGUIR_BOLA = 85
  MANTER_POSICAO_DEFENSIVA = 15

Outro jogador com bola:
  INTERCEPTAR = 100

Eu com bola:
  AGIR_COM_BOLA = 100
```

`SeletorDecisaoPonderada` deve:

- ignorar pesos menores ou iguais a zero;
- retornar a unica opcao quando so houver uma opcao valida;
- retornar `MANTER_POSICAO` quando nao houver opcoes validas;
- sortear com base nos pesos quando houver mais de uma opcao.

## Fora de Escopo

- Integrar no `JogadorAgent`.
- Criar probabilidades dinamicas complexas.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Classes compilam.
- Testes unitarios do seletor cobrem peso unico, pesos invalidos e sorteio ponderado.

## Resultado

- Criado `PerfilTatico.equilibrado()` com os pesos definidos para bola livre, outro jogador com bola e jogador com bola.
- Criado `SeletorDecisaoPonderada` com suporte a `Random` injetavel.
- O seletor ignora pesos invalidos, retorna a unica opcao valida, retorna `MANTER_POSICAO` sem opcoes validas e sorteia por peso.
- Esta task nao integrou a decisao no `JogadorAgent`, conforme fora de escopo.

## Validacao Executada

```powershell
mvn test
```

Resultado: `BUILD SUCCESS`, com 5 testes executados.
