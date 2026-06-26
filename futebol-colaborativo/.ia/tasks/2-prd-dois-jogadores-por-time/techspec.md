# TechSpec: Dois Jogadores por Time (2x2)

> **Status:** rascunho tecnico
> **PRD:** `prd.md`
> **Escopo:** expansao do bootstrap para 4 agentes com perfis taticos e filtro de disputa por time

---

## 1. Resumo Executivo

Esta TechSpec detalha as mudancas tecnicas para a simulacao 2x2. A arquitetura OODA do
`JogadorAgent` ja suporta `PerfilTatico`, `PapelJogador` e `Time` — esta entrega e a
primeira vez que esses conceitos influenciam o comportamento observavel da simulacao.

As mudancas sao em tres camadas:

1. **Backend — perfis:** adicionar `PerfilTatico.atacante()` e `PerfilTatico.zagueiro()`.
2. **Backend — bootstrap e filtro:** criar os 4 agentes e filtrar disputa por time.
3. **Frontend — visual:** inferir time pelo nome e colorir circulos corretamente.

---

## 2. Estado Atual Relevante

- `PerfilTatico` existe com apenas `equilibrado()`.
- `JogadorAgent` recebe `PerfilTatico` via `setup` e usa no `ControladorDecisaoJogador`.
- `App.java` cria apenas `azul` e `vermelho`.
- `SistemaFutebol.localizarOponenteProximoParaDisputa` nao filtra por time.
- `SistemaFutebol.configuracoes` ja armazena `ConfiguracaoJogador` com `getTime()` para cada jogador.
- `PlayerLayer.ts` ja tem `getTeamColor(team)` para azul (`A`) e vermelho (`B`), mas nao e acionado porque o payload atual nao envia `team`.
- O frontend infere team como `'unknown'` para o formato `legacy-nested`, resultando no circulo laranja/amarelo.

---

## 3. Arquitetura das Mudancas

### 3.1. Novos perfis em `PerfilTatico`

Adicionar dois metodos fabrica ao lado de `equilibrado()`:

```java
// Atacante: persegue a bola com mais frequencia quando ela esta livre
public static PerfilTatico atacante() { ... }

// Zagueiro: prefere manter posicao defensiva quando a bola esta livre
public static PerfilTatico zagueiro() { ... }
```

Pesos definidos:

| Situacao | Decisao | Atacante | Zagueiro |
|---|---|---|---|
| Bola livre | PERSEGUIR_BOLA | 75 | 25 |
| Bola livre | MANTER_POSICAO_DEFENSIVA | 25 | 75 |
| Outro com bola | INTERCEPTAR | 100 | 100 |
| Eu com bola | AGIR_COM_BOLA | 100 | 100 |

Justificativa: atacante e zagueiro compartilham os mesmos ramos deterministicos
(interceptar, agir com bola). A diferenca fica so no caso de bola livre, que e o
unico ramo probabilistico na arquitetura atual.

### 3.2. Posicionamento inicial dos 4 agentes

Campo: largura=100, altura=60, golY=30.

| Agente | x | y | golX | Time | Papel |
|---|---|---|---|---|---|
| azul-atacante | 45 | 30 | 100 | AZUL | ATACANTE |
| azul-zagueiro | 20 | 30 | 100 | AZUL | ZAGUEIRO |
| vermelho-atacante | 55 | 30 | 0 | VERMELHO | ATACANTE |
| vermelho-zagueiro | 80 | 30 | 0 | VERMELHO | ZAGUEIRO |

Logica de posicionamento:
- Atacante comeca proximo ao meio-campo, no proprio lado (azul no lado esquerdo, vermelho no direito).
- Zagueiro comeca proximo ao proprio gol.
- Y central (30) para todos no inicio.

### 3.3. Filtro de disputa por time em `SistemaFutebol`

`localizarOponenteProximoParaDisputa` atualmente itera todos os jogadores sem checar time.

Mudanca: antes de considerar um candidato a oponente, verificar se ele e de time diferente
usando `configuracoes.get(nomeOponente).getTime()` versus `configuracoes.get(nome).getTime()`.

```java
// dentro do loop de localizarOponenteProximoParaDisputa:
ConfiguracaoJogador confAtual = configuracoes.get(nome);
ConfiguracaoJogador confOponente = configuracoes.get(nomeOponente);

if (confAtual == null || confOponente == null) continue;
if (confAtual.getTime() == confOponente.getTime()) continue; // aliado, ignorar
```

Nenhuma mudanca de assinatura do metodo. Nenhum campo novo em `JogadorEstado`.

### 3.4. Bootstrap no `App.java`

Substituir a criacao de `azul` e `vermelho` por 4 agentes com os valores da tabela da secao 3.2.

```java
sistema.criarJogador(new ConfiguracaoJogador(
    "azul-atacante", 45, 30, Ambiente.largura, Time.AZUL, PapelJogador.ATACANTE));
sistema.criarJogador(new ConfiguracaoJogador(
    "azul-zagueiro", 20, 30, Ambiente.largura, Time.AZUL, PapelJogador.ZAGUEIRO));
sistema.criarJogador(new ConfiguracaoJogador(
    "vermelho-atacante", 55, 30, 0, Time.VERMELHO, PapelJogador.ATACANTE));
sistema.criarJogador(new ConfiguracaoJogador(
    "vermelho-zagueiro", 80, 30, 0, Time.VERMELHO, PapelJogador.ZAGUEIRO));
```

O `JogadorAgent` ja recebe `PapelJogador` via `ConfiguracaoJogador` e ja seleciona o perfil
pelo papel — mas hoje so usa `PerfilTatico.equilibrado()` independente do papel. Precisa
mudar o `setup` para selecionar o perfil correto:

```java
// em JogadorAgent.setup(), substituir:
perfilTatico = PerfilTatico.equilibrado();

// por:
perfilTatico = switch (papel) {
    case ATACANTE -> PerfilTatico.atacante();
    case ZAGUEIRO -> PerfilTatico.zagueiro();
    default -> PerfilTatico.equilibrado();
};
```

### 3.5. Frontend — inferencia de time pelo nome

O frontend hoje usa o formato `legacy-nested` (mapa de jogadores sem campo `team`).
Em vez de mudar o payload do backend, o frontend infere o time pelo prefixo do nome:

Em `normalizers.ts`, na funcao `normalizeLegacyPlayer`:

```typescript
// substituir:
team: 'unknown',

// por:
team: inferTeamFromId(id),
```

Nova funcao utilitaria (no mesmo arquivo):

```typescript
function inferTeamFromId(id: string): TeamId {
  if (id.startsWith('azul')) return 'A'
  if (id.startsWith('vermelho')) return 'B'
  return 'unknown'
}
```

Com isso, `PlayerLayer.getTeamColor` ja funciona corretamente para azul e vermelho
sem nenhuma outra mudanca no frontend.

O nome do jogador ja e exibido como label acima do circulo (`node.label.text = player.id`),
entao `azul-atacante`, `azul-zagueiro`, etc. aparecem automaticamente.

---

## 4. Arquivos Alterados

| Arquivo | Mudanca |
|---|---|
| `estrategia/PerfilTatico.java` | Adicionar `atacante()` e `zagueiro()` |
| `App.java` | Substituir 2 agentes por 4 |
| `agentes/JogadorAgent.java` | Selecionar perfil baseado no papel no `setup` |
| `SistemaFutebol.java` | Filtrar aliados em `localizarOponenteProximoParaDisputa` |
| `frontend/src/game/model/normalizers.ts` | Inferir team pelo prefixo do nome |

**Nao sao alterados:**
- `JogadorEstado.java` — payload WebSocket permanece identico
- `ContextoDecisao.java` — ja tem `time` e `papel`
- `ControladorDecisaoJogador.java` — logica de decisao nao muda
- `PlayerLayer.ts` — ja tem `getTeamColor` pronto para `A` e `B`

---

## 5. Invariantes e Protecoes

- Apenas jogadores de times diferentes podem entrar em disputa.
- Cada jogador continua com apenas um `PerfilTatico` imutavel durante a partida.
- O payload do WebSocket nao muda — o frontend continua compativel com o formato atual.
- Apos gol, todos os jogadores voltam para suas posicoes iniciais (ja implementado em `registrarGol`).

---

## 6. Estrategia de Testes

### 6.1. Testes unitarios

- `PerfilTaticoTest`: verificar que `atacante()` tem peso de `PERSEGUIR_BOLA` maior que `zagueiro()`.
- `PerfilTaticoTest`: verificar que `zagueiro()` tem peso de `MANTER_POSICAO_DEFENSIVA` maior que `atacante()`.
- `SistemaFutebolTest` (novo ou existente): verificar que `localizarOponenteProximoParaDisputa` nao retorna aliados.

### 6.2. Validacao manual

```powershell
cd futebol-colaborativo
mvn exec:java
```

Em outro terminal:
```powershell
cd frontend
npm run dev
```

Verificar:
- 4 agentes criados (logs no terminal).
- 4 circulos no campo, 2 azuis e 2 vermelhos.
- Nomes corretos visiveis acima dos circulos.
- Nenhuma disputa entre aliados (verificar logs: nenhum CFP de `azul-*` para `azul-*`).
- Disputa normal entre adversarios continua ocorrendo.
- Gol ainda e registrado e jogadores voltam ao inicio.

---

## 7. Sequencia de Implementacao (Tasks)

1. Criar `PerfilTatico.atacante()` e `PerfilTatico.zagueiro()` + testes unitarios.
2. Bootstrap dos 4 agentes no `App.java` + selecao de perfil por papel no `JogadorAgent`.
3. Filtrar aliados em `SistemaFutebol.localizarOponenteProximoParaDisputa`.
4. Frontend: inferir time pelo prefixo do nome em `normalizers.ts`.
5. Validacao de regressao 2x2 (manual).

---

## 8. Decisoes e Trade-offs

| Decisao | Alternativa descartada | Motivo |
|---|---|---|
| Inferir time pelo prefixo do nome no frontend | Adicionar `time` ao `JogadorEstado` e mudar payload | Evita mudanca no payload do WebSocket e no formato de dados |
| Pesos distintos apenas no caso de bola livre | Pesos distintos em todos os ramos | Os outros ramos (interceptar, agir com bola) nao tem variacao probabilistica na arquitetura atual |
| Filtrar aliados em `localizarOponenteProximoParaDisputa` | Filtrar no `JogadorAgent` | O sistema ja tem `configuracoes` com time; centralizar no `SistemaFutebol` evita duplicacao |

---

## 9. Extensibilidade (sem implementar agora)

Decisoes que nao bloqueiam o PRD 3:

- `ContextoDecisao` ja carrega `time` e `papel` — o controlador pode usar para decisoes coletivas futuras.
- `PerfilTatico` aceita qualquer `Map<TipoDecisao, Integer>` — novos pesos (passe, cobertura) podem ser adicionados sem reescrever a classe.
- O `TipoDecisao` pode receber `PASSAR_BOLA`, `COBRIR_POSICAO` sem quebrar o switch atual.
- `JogadorEstado` pode receber `time` no futuro sem quebrar o payload atual (campo novo e ignorado pelo frontend legado).
