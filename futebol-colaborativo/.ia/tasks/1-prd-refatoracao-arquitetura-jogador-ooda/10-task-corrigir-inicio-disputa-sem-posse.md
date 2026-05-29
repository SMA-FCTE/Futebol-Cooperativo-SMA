# Task 10: Corrigir Inicio de Disputa Sem Posse de Bola

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Problema

Quando dois jogadores estao proximos e nenhum esta com a bola, o metodo `deveIniciarDisputa`
usa uma comparacao de nomes como desempate e permite que um deles inicie disputa.
Disputa de bola nao faz sentido quando nenhum agente esta com a bola.

## Regra correta

Disputa so deve ser iniciada quando:
- o oponente esta com a bola, E
- o jogador atual nao esta com a bola.

O agente sem a bola e quem busca a disputa. O agente com a bola nao inicia.

## O que sera alterado

Arquivo: `JogadorAgent.java`, metodo `deveIniciarDisputa`.

**Antes:**
```java
private boolean deveIniciarDisputa(SistemaFutebol.OponenteDisputa oponente) {
    if (!estado.comBola && oponente.comBola) {
        return true;
    }

    if (estado.comBola && !oponente.comBola) {
        return false;
    }

    return getLocalName().compareTo(oponente.nome) < 0;
}
```

**Depois:**
```java
private boolean deveIniciarDisputa(SistemaFutebol.OponenteDisputa oponente) {
    return !estado.comBola && oponente.comBola;
}
```

## Fora de Escopo

- Alterar o protocolo ACL da disputa.
- Alterar `localizarOponenteProximoParaDisputa` no `SistemaFutebol`.
- Alterar qualquer outro arquivo.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

## Criterios de Sucesso

- Codigo compila.
- Testes passam.
- Dois jogadores proximos sem nenhum ter a bola nao iniciam disputa.
- O jogador sem a bola que esta perto do jogador com a bola continua iniciando disputa normalmente.

## Resultado

- `deveIniciarDisputa` simplificado para uma unica condicao: `!estado.comBola && oponente.comBola`.
- Removido o terceiro caso que usava comparacao de nomes como desempate e permitia disputa sem posse.
