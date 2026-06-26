# Task 7: Integrar Ciclo OODA no JogadorAgent

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`

## Objetivo

Substituir a decisao direta do `JogadorAgent` pelo fluxo:

```text
montar ContextoDecisao -> decidir TipoDecisao -> executar acao
```

## Escopo

Alterar apenas o necessario em `JogadorAgent` para:

- inicializar `PerfilTatico`, `PapelJogador`, `Time` e `ControladorDecisaoJogador`;
- manter contador `ticksBolaLivre`;
- criar `montarContextoDecisao`;
- criar `executarDecisao`;
- substituir `decidirAcaoPrincipal` pelo fluxo OODA.

## Mapeamento de Execucao

```text
PERSEGUIR_BOLA -> agirSemJogadorComBola()
AGIR_COM_BOLA -> agirComBola()
INTERCEPTAR -> agirSemBola(contexto.getJogadorComBola())
MANTER_POSICAO_DEFENSIVA -> mover para uma posicao simples a frente do proprio gol
MANTER_POSICAO -> nao mover
```

## Fora de Escopo

- Extrair disputa ACL.
- Extrair interceptacao.
- Alterar payload WebSocket.
- Alterar frontend.

## Validacao

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

## Criterios de Sucesso

- Simulacao 1x1 continua funcionando.
- Disputa de bola continua funcionando.
- Jogador usa controlador para decidir.
- Probabilidade so afeta bola livre.

## Resultado

- `JogadorAgent` agora inicializa `PerfilTatico.equilibrado()` e `ControladorDecisaoJogador`.
- `decidirAcaoPrincipal()` passou a montar `ContextoDecisao`, chamar o controlador e executar a `TipoDecisao`.
- Adicionado contador `ticksBolaLivre` no agente.
- `PERSEGUIR_BOLA`, `AGIR_COM_BOLA`, `INTERCEPTAR`, `MANTER_POSICAO_DEFENSIVA` e `MANTER_POSICAO` foram mapeadas para acoes concretas.
- A probabilidade do perfil tatico entra apenas no caso de bola livre.
- Penalidade, disputa ACL, posse, chute e atualizacao de estado foram preservados no fluxo existente.

## Validacao Executada

```powershell
mvn test
mvn clean test
```

Resultado: `BUILD SUCCESS`, com 10 testes executados. A validacao manual com `mvn exec:java` fica para a task de regressao, porque inicia JADE/API/WebSocket e permanece rodando.
