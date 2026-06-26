# Task 6: Placar da Partida

> **Status:** pending
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md` — Seção 5
> **Depende de:** Tasks 3 (área de gol) e 4 (layout frontend)

---

## Objetivo

Exibir o placar (gols por time) no frontend, dentro do card do campo, centralizado
acima do canvas, no formato `AZUL  N × M  VERMELHO`.

Hoje gols são registrados apenas no log do terminal. Nenhum contador é mantido e
nada é exibido na UI.

---

## Contexto relevante

| Arquivo | Estado atual |
|---------|-------------|
| `SistemaFutebol.java` | `registrarGol()` reseta campo mas não incrementa contador |
| `SistemaFutebol.java` | `enviarEstadoAtualParaClientes()` envia `jogadores`, `bola`, `disputa`, `passe` — sem `placar` |
| `normalizers.ts` | `normalizeLegacyNestedPayload` retorna `scoreboard: { ...DEFAULT_SCOREBOARD }` (zeros fixos) |
| `gameTypes.ts` | Tipo `Scoreboard = { A: number, B: number }` já existe |
| `gameState.ts` | `DEFAULT_SCOREBOARD = { A: 0, B: 0 }` já existe |
| `normalizers.ts` | `normalizeScoreboard(value)` já existe — parseia `{A, B}` ou retorna default |
| `App.tsx` | `section.viewport-panel` contém `panel-heading`, `GameViewport`, `MatchStatePanel` |

**Convenção de times:**
- Time **AZUL** ataca `golX = Ambiente.largura` (lado direito) → é o `"A"` no payload
- Time **VERMELHO** ataca `golX = 0` (lado esquerdo) → é o `"B"` no payload
- Gol no lado esquerdo (`bola.x ≤ 0`) → VERMELHO marcou
- Gol no lado direito (`bola.x ≥ largura`) → AZUL marcou

---

## Alterações

### 1. `futebol-colaborativo/src/main/java/com/futebol/colaborativo/SistemaFutebol.java`

#### 1.1 Adicionar contadores (no topo da classe, junto dos outros campos privados)

```java
private int golsTimeAzul    = 0;
private int golsTimeVermelho = 0;
```

#### 1.2 Substituir `verificarGolDaBola()` — incrementar antes de registrar

```java
// antes
private void verificarGolDaBola() {
    boolean emFaixaGol = Ambiente.bola.y >= Ambiente.golYMin
                      && Ambiente.bola.y <= Ambiente.golYMax;
    boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0   && emFaixaGol;
    boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura && emFaixaGol;
    if (!bolaNoGolEsquerdo && !bolaNoGolDireito) return;
    String marcador = localizarUltimoAtacante(bolaNoGolEsquerdo ? 0 : Ambiente.largura);
    registrarGol(marcador == null ? "Bola" : marcador);
}

// depois
private void verificarGolDaBola() {
    boolean emFaixaGol = Ambiente.bola.y >= Ambiente.golYMin
                      && Ambiente.bola.y <= Ambiente.golYMax;
    boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0   && emFaixaGol;
    boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura && emFaixaGol;

    if (!bolaNoGolEsquerdo && !bolaNoGolDireito) {
        return;
    }

    if (bolaNoGolEsquerdo) {
        golsTimeVermelho++;
    } else {
        golsTimeAzul++;
    }

    String marcador = localizarUltimoAtacante(bolaNoGolEsquerdo ? 0 : Ambiente.largura);
    registrarGol(marcador == null ? "Bola" : marcador);
}
```

#### 1.3 Substituir `enviarEstadoAtualParaClientes()` — adicionar campo `placar`

```java
// antes
private void enviarEstadoAtualParaClientes() {
    Map<String, Object> resposta = new HashMap<>();
    resposta.put("jogadores", estados);
    resposta.put("bola", Ambiente.bola);
    resposta.put("disputa", ultimaDisputa);
    resposta.put("passe", ultimoPasse);

    String json = gson.toJson(resposta);
    EventSocket.enviarMensagemParaClientes(json);
}

// depois
private void enviarEstadoAtualParaClientes() {
    Map<String, Object> resposta = new HashMap<>();
    resposta.put("jogadores", estados);
    resposta.put("bola", Ambiente.bola);
    resposta.put("disputa", ultimaDisputa);
    resposta.put("passe", ultimoPasse);

    Map<String, Integer> placar = new HashMap<>();
    placar.put("A", golsTimeAzul);
    placar.put("B", golsTimeVermelho);
    resposta.put("placar", placar);

    String json = gson.toJson(resposta);
    EventSocket.enviarMensagemParaClientes(json);
}
```

---

### 2. `frontend/src/game/model/normalizers.ts`

Localizar o bloco `return createSuccess` dentro de `normalizeLegacyNestedPayload` (por volta da linha 104).

```ts
// antes
return createSuccess({
  payloadFormat: 'legacy-nested',
  players,
  ball,
  disputa,
  passe,
  tempo: null,
  scoreboard: { ...DEFAULT_SCOREBOARD },
})

// depois
return createSuccess({
  payloadFormat: 'legacy-nested',
  players,
  ball,
  disputa,
  passe,
  tempo: null,
  scoreboard: normalizeScoreboard((raw as { placar?: unknown }).placar),
})
```

`normalizeScoreboard` já está no mesmo arquivo (linha ~350) e já trata valores ausentes.

---

### 3. `frontend/src/app/App.tsx`

Adicionar o elemento de placar entre `div.panel-heading` e `<GameViewport>` dentro da `section.viewport-panel`:

```tsx
// antes
<section className="panel viewport-panel">
  <div className="panel-heading">
    <div>
      <span className="panel-kicker">Visualização</span>
      <h2>Campo da partida</h2>
    </div>
  </div>

  <GameViewport state={gameState} />
  <MatchStatePanel state={gameState} />
</section>

// depois
<section className="panel viewport-panel">
  <div className="panel-heading">
    <div>
      <span className="panel-kicker">Visualização</span>
      <h2>Campo da partida</h2>
    </div>
  </div>

  <div className="match-score">
    <span className="match-score-team match-score-azul">Azul</span>
    <strong className="match-score-value">
      {gameState.scoreboard.A} × {gameState.scoreboard.B}
    </strong>
    <span className="match-score-team match-score-vermelho">Vermelho</span>
  </div>

  <GameViewport state={gameState} />
  <MatchStatePanel state={gameState} />
</section>
```

Nenhuma importação nova necessária — `gameState.scoreboard` já está disponível no escopo.

---

### 4. `frontend/src/styles/globals.css`

Adicionar os estilos do placar após o bloco `.panel-heading` (por volta da linha 214):

```css
.match-score {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.match-score-team {
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.match-score-azul {
  color: rgba(96, 165, 250, 0.9);
}

.match-score-vermelho {
  color: rgba(248, 113, 113, 0.9);
}

.match-score-value {
  font-family: Georgia, 'Times New Roman', serif;
  font-size: clamp(1.8rem, 3vw, 2.5rem);
  line-height: 1;
  letter-spacing: -0.02em;
}
```

---

## Arquivos alterados

| Arquivo | Mudança |
|---------|---------|
| `SistemaFutebol.java` | Contadores `golsTimeAzul`/`golsTimeVermelho`; incremento em `verificarGolDaBola()`; campo `placar` em `enviarEstadoAtualParaClientes()` |
| `frontend/src/game/model/normalizers.ts` | `normalizeLegacyNestedPayload` parseia `placar` em vez de retornar zeros |
| `frontend/src/app/App.tsx` | Elemento `.match-score` acima do canvas |
| `frontend/src/styles/globals.css` | Classes `.match-score*` |

---

## Checklist de verificação

- [ ] `mvn compile` sem erros
- [ ] `npm run lint` sem erros
- [ ] Backend rodando: payload WebSocket inclui `"placar":{"A":0,"B":0}` (verificar no painel Diagnóstico)
- [ ] Frontend: placar `Azul 0 × 0 Vermelho` aparece centralizado acima do canvas ao conectar
- [ ] Após a bola entrar no gol direito (lado do time VERMELHO): contador A incrementa → placar atualiza na UI
- [ ] Após a bola entrar no gol esquerdo (lado do time AZUL): contador B incrementa → placar atualiza na UI
- [ ] Placar não reseta ao marcar gol — apenas ao reiniciar o backend
- [ ] Cores corretas: "Azul" em azul-claro, "Vermelho" em vermelho-claro

---

## Fora de escopo

- Persistência do placar entre reinicializações do backend.
- Nome customizado dos times.
- Histórico de gols ou quem marcou cada gol.
- Animação ao marcar gol.

---

## Resultado

> Preencher após execução.
