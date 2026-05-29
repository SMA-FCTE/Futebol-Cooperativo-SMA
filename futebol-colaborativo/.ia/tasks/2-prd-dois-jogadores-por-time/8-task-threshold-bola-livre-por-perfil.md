# Task 6: Threshold de Bola Livre por Perfil Tatico

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`
> **Depende de:** Task 1 (perfis criados), Task 2 (bootstrap com papeis)
> **Deve ser executada antes de:** Task 5 (validacao de regressao)

## Problema

O controlador de decisao usa uma constante fixa `TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR = 10`
para todos os jogadores. Quando um jogador sorteia `MANTER_POSICAO_DEFENSIVA`, ele fica
na posicao defensiva por ate 10 ticks antes de ser forcado a perseguir a bola. Isso cria
um vai-e-vem visivel: defensiva -> forcado a perseguir -> defensiva de novo.

Com perfis taticos distintos, o correto e que cada perfil tambem defina sua propria
paciencia antes de ser forcado a perseguir.

## Objetivo

Mover o threshold de bola livre para dentro do `PerfilTatico`, permitindo que
atacantes sejam forcados mais cedo e zagueiros mais tarde.

## Escopo

Alterar `PerfilTatico.java` e `ControladorDecisaoJogador.java`.

## Requisitos

### PerfilTatico.java

Adicionar campo e getter:

```java
private final int ticksBolaLivreParaForcar;

public int getTicksBolaLivreParaForcar() {
    return ticksBolaLivreParaForcar;
}
```

Valores por perfil:

| Perfil | ticksBolaLivreParaForcar | Justificativa |
|---|---|---|
| `atacante()` | 5 | Pouca paciencia: vai logo atras da bola |
| `zagueiro()` | 25 | Mais paciencia: fica em posicao defensiva por mais tempo |
| `equilibrado()` | 10 | Mantem o comportamento atual |

### ControladorDecisaoJogador.java

Substituir a constante fixa pelo valor do perfil:

```java
// antes:
if (contexto.getTicksBolaLivre() >= TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR) {

// depois:
if (contexto.getTicksBolaLivre() >= contexto.getPerfilTatico().getTicksBolaLivreParaForcar()) {
```

A constante `TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR` pode ser removida.

## Testes a atualizar

Em `ControladorDecisaoJogadorTest`: os testes que usam `TICKS_BOLA_LIVRE_FORCAR_PERSEGUIR`
devem ser atualizados para usar o valor do perfil correto nos contextos de teste.

## Fora de Escopo

- Alterar qualquer outro arquivo.
- Alterar os pesos dos perfis.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

## Criterios de Sucesso

- Codigo compila.
- Testes passam.
- Atacante para de ir para posicao defensiva tao frequentemente (threshold menor).
- Zagueiro permanece mais tempo em posicao defensiva (threshold maior).
