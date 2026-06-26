# TechSpec: Campo Real e Área de Gol

> **PRD:** `prd.md`
> **Versão:** 0.1

---

## 1. Contexto técnico

### 1.1. Campo atual

`Ambiente.java` define o campo como constantes estáticas:

```java
public static int largura = 100;
public static int altura  = 60;
public static int golY    = altura / 2;   // 30
```

`Movimento.java` usa essas dimensões em `limitarAoCampo`:

```java
estado.x = limitar(estado.x, 0, Ambiente.largura);
estado.y = limitar(estado.y, 0, Ambiente.altura);
```

O código do campo é totalmente baseado em `Ambiente.largura` e `Ambiente.altura` —
não existem números mágicos de dimensão de campo espalhados no código fora de `App.java`.

### 1.2. Detecção de gol atual

`SistemaFutebol.verificarGolDaBola()` valida gol por raio em torno de um ponto:

```java
private static final double RAIO_GOL = 2.4;

boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
```

Isso representa uma janela de 4.8 unidades (~8% de altura=60) ao redor do centro.
A área de gol desenhada no frontend tem `altura × 0.2016 ≈ 20%` de altura —
mais de duas vezes maior que a janela de detecção.

A ordem em `aplicarFisicaBola()` é:

```java
rebaterBolaNasBordas();   // (1) rebate antes
verificarGolDaBola();      // (2) depois verifica gol
```

Com `x ≤ 0` (após rebate) e `y` no range, o gol é detectado e `registrarGol()` reseta.
Essa ordem se mantém sem alteração — não é necessário trocar as chamadas.

### 1.3. Constantes de jogo dependentes do tamanho do campo

Todas as distâncias e forças abaixo foram calibradas para campo 100×60.
Com campo 300×150 (fator 3× em largura), precisam escalar na mesma proporção.

| Constante | Arquivo | Valor atual | Valor novo | Motivo |
|-----------|---------|-------------|------------|--------|
| `DISTANCIA_CHUTE_AO_GOL` | `JogadorAgent` | 10.0 | 30.0 | Distância de campo para chutar |
| `DISTANCIA_POSICAO_DEFENSIVA_DO_GOL` | `JogadorAgent` | 8.0 | 24.0 | Posição do zagueiro perto do próprio gol |
| `DISTANCIA_POSICAO_OFENSIVA_DO_GOL` | `JogadorAgent` | 35.0 | 105.0 | Posição do atacante perto do gol adversário |
| `FORCA_CHUTE` | `JogadorAgent` | 1.65 | 5.0 | Bola precisa cruzar o campo maior |
| `FORCA_CHUTE_DIRIGIDO` | `JogadorAgent` | 1.35 | 4.0 | Idem |
| `FORCA_PASSE` | `JogadorAgent` | 1.0 | 3.0 | Bola precisa chegar ao aliado |
| `RAIO_CONTATO_BOLA` | `Movimento` | 1.2 | 3.6 | Raio de toque na bola |
| `RAIO_PERSEGUICAO_BOLA` | `Movimento` | 10.0 | 30.0 | Raio de perseguição de bola livre |
| `RAIO_GOL` | `Movimento` | 2.0 | 6.0 | Raio de "chegar perto do gol" para chutar |
| `velocidade` (default) | `JogadorEstado` | 1.0 | 3.0 | Campo 3× maior; manter tempo de travessia |

> **Nota:** `RAIO_PASSE` em `SistemaFutebol` (100.0) já cobre todo o campo antigo —
> com campo 300× pode ser mantido em 100.0 (raio mais seletivo), ou ajustado.
> Decisão: manter em 100.0 por enquanto; a task de validação calibra se necessário.

### 1.4. Posições iniciais em `App.java`

Posições hardcoded para campo 100×60 reescaladas para 300×150:

| Constante | Valor atual | Valor novo | Escala |
|-----------|-------------|------------|--------|
| `Y_INICIAL` | 30 | 75 | ×2.5 |
| `AZUL_ATACANTE_X` | 45 | 135 | ×3 |
| `AZUL_ZAGUEIRO_X` | 20 | 60 | ×3 |
| `VERMELHO_ATACANTE_X` | 55 | 165 | ×3 |
| `VERMELHO_ZAGUEIRO_X` | 80 | 240 | ×3 |

### 1.5. Frontend — dimensões de campo hardcoded

`gameState.ts` declara:

```ts
export const DEFAULT_FIELD_DIMENSIONS = {
  width: 100,
  height: 60,
} as const
```

Esse valor é usado como estado inicial de `field` no `GameState`.
O `coordinateMapper` escala coordenadas de campo para pixels com:

```ts
const scale = Math.min(innerWidth / field.width, innerHeight / field.height)
```

Isso significa que alterar `DEFAULT_FIELD_DIMENSIONS` para `{width: 300, height: 150}`
faz o mapeamento de coordenadas se ajustar automaticamente — sem nenhuma outra mudança
na camada de renderização.

### 1.6. Frontend — layout atual

`App.tsx`:
```tsx
<main className="dashboard">
  <section className="panel viewport-panel"> {/* campo + MatchStatePanel */}
    ...
  </section>
  <aside className="sidebar">           {/* Scoreboard + DebugPanel */}
    ...
  </aside>
</main>
```

`globals.css`:
```css
.dashboard {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.95fr);
  gap: 20px;
}
.sidebar {
  display: grid;
  gap: 20px;
}
```

A mudança desejada: campo ocupa 100% da largura superior; Scoreboard e DebugPanel
ficam lado a lado em uma linha abaixo.

---

## 2. Task 2 — Aumentar dimensões do campo

### 2.1. `Ambiente.java`

Alterar `largura` e `altura`. Adicionar `golYMin` e `golYMax` como campos estáticos
calculados na classe (não em `SistemaFutebol`) para que qualquer componente que precisar
da área do gol possa ler direto:

```java
public static int largura = 300;
public static int altura  = 150;
public static Bola bola   = new Bola();
public static int golX    = largura;
public static int golY    = altura / 2;    // 75

// Fração da altura usada pelo frontend para desenhar a goalArea: 0.36 × 0.56
private static final double FRACAO_ALTURA_GOL = 0.36 * 0.56;
public static double golYMin = golY - (altura * FRACAO_ALTURA_GOL / 2);  // ≈ 59.88
public static double golYMax = golY + (altura * FRACAO_ALTURA_GOL / 2);  // ≈ 90.12
```

> `golX` e `golY` já são usados em outros pontos; a semântica não muda.
> `golYMin/golYMax` são novos campos — não quebram nenhuma referência existente.

### 2.2. `App.java`

Atualizar as cinco constantes de posição:

```java
private static final double Y_INICIAL            = 75;
private static final double AZUL_ATACANTE_X      = 135;
private static final double AZUL_ZAGUEIRO_X      = 60;
private static final double VERMELHO_ATACANTE_X  = 165;
private static final double VERMELHO_ZAGUEIRO_X  = 240;
```

Não há outras mudanças em `App.java` — `Ambiente.largura` é referenciado diretamente
nos construtores de `ConfiguracaoJogador` (time AZUL usa `Ambiente.largura` como `golX`,
time VERMELHO usa `0`), e esse valor é lido dinamicamente.

### 2.3. `JogadorEstado.java`

Atualizar o valor default de velocidade:

```java
public double velocidade = 3.0;
```

### 2.4. `JogadorAgent.java`

Atualizar as constantes de distância e força:

```java
private static final double DISTANCIA_CHUTE_AO_GOL              = 30.0;
private static final double FORCA_CHUTE                          = 5.0;
private static final double FORCA_CHUTE_DIRIGIDO                 = 4.0;
private static final double DISTANCIA_POSICAO_DEFENSIVA_DO_GOL   = 24.0;
private static final double DISTANCIA_POSICAO_OFENSIVA_DO_GOL    = 105.0;
private static final double FORCA_PASSE                          = 3.0;
```

### 2.5. `Movimento.java`

Atualizar os três raios:

```java
public static final double RAIO_CONTATO_BOLA     = 3.6;
public static final double RAIO_PERSEGUICAO_BOLA = 30.0;
public static final double RAIO_GOL              = 6.0;
```

### 2.6. `gameState.ts` (frontend)

```ts
export const DEFAULT_FIELD_DIMENSIONS = {
  width: 300,
  height: 150,
} as const
```

### 2.7. Arquivos alterados nesta task

| Arquivo | Mudança |
|---------|---------|
| `model/Ambiente.java` | `largura=300`, `altura=150`; campos `golYMin`, `golYMax` |
| `App.java` | Novas posições iniciais dos jogadores |
| `model/JogadorEstado.java` | `velocidade = 3.0` |
| `agentes/JogadorAgent.java` | Constantes de distância e força (×3) |
| `movimento/Movimento.java` | Raios de contato, perseguição e gol (×3) |
| `frontend/src/game/model/gameState.ts` | `DEFAULT_FIELD_DIMENSIONS` → 300×150 |

---

## 3. Task 3 — Área de gol real

### 3.1. `SistemaFutebol.java`

Remover `RAIO_GOL` (campo `private static final double RAIO_GOL = 2.4`).

Substituir a lógica de detecção em `verificarGolDaBola()`:

```java
// antes
boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;

// depois
boolean emFaixaGol = Ambiente.bola.y >= Ambiente.golYMin
                  && Ambiente.bola.y <= Ambiente.golYMax;
boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0   && emFaixaGol;
boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura && emFaixaGol;
```

A ordem de chamada em `aplicarFisicaBola()` não muda:

```java
rebaterBolaNasBordas();
verificarGolDaBola();
```

O rebate ocorre antes, mas `verificarGolDaBola()` detecta gol quando `x <= 0`
(x foi fixado em 0 pelo rebate) e y está na faixa → gol é registrado e bola é resetada.
A inversão de velocidade pelo rebate não tem efeito porque `registrarGol()` posiciona
a bola no centro imediatamente.

### 3.2. Arquivos alterados nesta task

| Arquivo | Mudança |
|---------|---------|
| `SistemaFutebol.java` | Remover `RAIO_GOL`; usar `Ambiente.golYMin/golYMax` em `verificarGolDaBola()` |

> `Ambiente.golYMin` e `Ambiente.golYMax` são criados na Task 2 — esta task depende dela.

---

## 4. Task 4 — Layout frontend

### 4.1. `App.tsx`

Substituir `<aside className="sidebar">` por `<div className="panels-row">`:

```tsx
// antes
<main className="dashboard">
  <section className="panel viewport-panel">
    ...
  </section>
  <aside className="sidebar">
    <Scoreboard ... />
    <DebugPanel ... />
  </aside>
</main>

// depois
<main className="dashboard">
  <section className="panel viewport-panel">
    ...
  </section>
  <div className="panels-row">
    <Scoreboard ... />
    <DebugPanel ... />
  </div>
</main>
```

### 4.2. `globals.css`

Alterar `.dashboard` para coluna única:

```css
/* antes */
.dashboard {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.95fr);
  gap: 20px;
  align-items: start;
}

/* depois */
.dashboard {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
```

Remover o seletor `.sidebar` e adicionar `.panels-row`:

```css
/* remover */
.sidebar {
  display: grid;
  gap: 20px;
}

/* adicionar */
.panels-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  align-items: start;
}
```

Aumentar altura mínima do viewport para aproveitar mais da tela com o campo maior:

```css
/* antes */
.viewport-panel {
  min-height: 720px;
  ...
}

/* depois */
.viewport-panel {
  min-height: 580px;  /* menor que antes — campo já tem mais espaço com coluna única */
  ...
}
```

Atualizar o breakpoint de `max-width: 1100px` para colapsar `.panels-row`:

```css
@media (max-width: 1100px) {
  .panels-row {
    grid-template-columns: 1fr;
  }
  /* .dashboard já é 1 coluna — sem mudança necessária */
}
```

### 4.3. Arquivos alterados nesta task

| Arquivo | Mudança |
|---------|---------|
| `frontend/src/app/App.tsx` | `aside.sidebar` → `div.panels-row` |
| `frontend/src/styles/globals.css` | `.dashboard` coluna única; `.sidebar` → `.panels-row` 2 colunas; ajuste `viewport-panel`; breakpoint responsive |

---

## 5. Task 5 — Placar da partida

### 5.1. `SistemaFutebol.java`

Adicionar dois contadores de gol como campos da classe (inicializam em 0):

```java
private int golsTimeAzul    = 0;
private int golsTimeVermelho = 0;
```

Em `verificarGolDaBola()`, incrementar o contador do time correto antes de chamar `registrarGol()`.
A lógica de atribuição de time vem da convenção de `golX`:
- Gol no lado esquerdo (`x ≤ 0`): time VERMELHO ataca esse lado (`golX = 0`) → `golsTimeVermelho++`
- Gol no lado direito (`x ≥ largura`): time AZUL ataca esse lado (`golX = largura`) → `golsTimeAzul++`

```java
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

Em `enviarEstadoAtualParaClientes()`, incluir `placar` no payload:

```java
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

> `"A"` = AZUL, `"B"` = VERMELHO. Convenção já usada pelo tipo `Scoreboard` do frontend
> e pelo normalizador `normalizeSnapshotPayload`.

### 5.2. `frontend/src/game/model/normalizers.ts`

`normalizeLegacyNestedPayload` hoje retorna `scoreboard: { ...DEFAULT_SCOREBOARD }` (sempre 0, 0).
Alterar para parsear o campo `placar` que o backend agora envia:

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

`normalizeScoreboard` já existe no arquivo e trata valores ausentes retornando `DEFAULT_SCOREBOARD`.

### 5.3. `frontend/src/app/App.tsx`

Adicionar o elemento de placar entre `panel-heading` e `<GameViewport>` dentro do `viewport-panel`:

```tsx
// dentro da section.viewport-panel, entre panel-heading e GameViewport
<div className="match-score">
  <span className="match-score-team match-score-azul">Azul</span>
  <strong className="match-score-value">
    {gameState.scoreboard.A} × {gameState.scoreboard.B}
  </strong>
  <span className="match-score-team match-score-vermelho">Vermelho</span>
</div>
```

Não há novo prop para passar — `gameState.scoreboard` já está disponível no escopo de `App`.

### 5.4. `frontend/src/styles/globals.css`

Adicionar os estilos do placar (inserir após `.panel-heading`):

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

### 5.5. Arquivos alterados nesta task

| Arquivo | Mudança |
|---------|---------|
| `SistemaFutebol.java` | Contadores `golsTimeAzul`/`golsTimeVermelho`; incremento em `verificarGolDaBola()`; `placar` no payload |
| `frontend/src/game/model/normalizers.ts` | `normalizeLegacyNestedPayload` lê `placar` em vez de retornar zeros |
| `frontend/src/app/App.tsx` | Elemento `.match-score` entre heading e canvas |
| `frontend/src/styles/globals.css` | Classes `.match-score`, `.match-score-team`, `.match-score-azul`, `.match-score-vermelho`, `.match-score-value` |

---

## 6. Sequenciamento das tasks

| Task | Título | Depende de |
|------|--------|-----------|
| 2 | Aumentar dimensões do campo (backend + gameState.ts) | — |
| 3 | Área de gol real | Task 2 (`Ambiente.golYMin/golYMax`) |
| 4 | Layout frontend | — (independente) |
| 5 | Validação e regressão PRD 4 | Tasks 2, 3 e 4 |
| 6 | Placar da partida | Tasks 3 e 4 concluídas |

---

## 7. Invariantes a preservar

- `Movimento.limitarAoCampo()` usa `Ambiente.largura` e `Ambiente.altura` diretamente — não precisa mudar.
- `ContextoDecisao.bolaNoFieldAdversario` usa `Ambiente.largura` diretamente — não precisa mudar.
- `CONVERSA_ID_DISPUTA` e `CONVERSA_ID_PASSE` — não alterados.
- `BolaAgent` — não alterado.
- O mapeamento de coordenadas do frontend (`coordinateMapper`) é 100% proporcional a `field.width/field.height` — nenhuma alteração necessária além de `DEFAULT_FIELD_DIMENSIONS`.
- O campo `placar` é **aditivo** no payload WebSocket — não quebra nenhuma leitura existente do frontend.

---

## 7. Riscos técnicos

| Risco | Mitigação |
|-------|-----------|
| Bola não chega ao gol com força insuficiente | As forças foram escaladas 3× — validar visualmente; ajustar em task de validação |
| Jogadores se sobrepõem nas posições iniciais pós-reset | Verificar `registrarGol()` — usa `configuracao.getXInicial()` que vem de `App.java` ✓ |
| `RAIO_PASSE` de 100 unidades pode ser muito seletivo no campo 300 | Monitorar passes na validação; ampliar se necessário |
| Campo visual muito pequeno ou muito grande na tela | Ajustar `min-height` de `.game-viewport`/`.viewport-panel` na task 4 se necessário |
| Gol detectado mesmo quando bola toca a linha lateral fora da área | A faixa Y é calculada por `golYMin/golYMax` — cobre exatamente a área desenhada; verificar visualmente |
