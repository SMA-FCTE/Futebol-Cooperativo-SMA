# Task 4: Layout Frontend — Campo em Destaque

> **Status:** pending
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** — (independente das Tasks 2 e 3)

---

## Problema

O layout atual divide a tela em duas colunas: campo à esquerda (~63%) e painéis
laterais à direita (~37%). Com o campo 300×150, a área de visualização fica comprimida
pela presença dos painéis ao lado. Os cards de "Estado da Jogada" e "Diagnóstico"
são secundários — devem aparecer abaixo do campo, não competir com ele pelo espaço.

Estrutura atual em `App.tsx`:

```tsx
<main className="dashboard">           {/* grid: 1.7fr + 0.95fr */}
  <section className="panel viewport-panel">
    <GameViewport .../>
    <MatchStatePanel .../>
  </section>
  <aside className="sidebar">          {/* coluna direita, empilhado */}
    <Scoreboard .../>
    <DebugPanel .../>
  </aside>
</main>
```

CSS atual:

```css
.dashboard {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.95fr);
  gap: 20px;
  align-items: start;
}
.sidebar {
  display: grid;
  gap: 20px;
}
```

---

## Objetivo

- O campo ocupa 100% da largura superior.
- "Estado da Jogada" (`Scoreboard`) e "Diagnóstico" (`DebugPanel`) ficam lado a lado
  em uma linha abaixo do campo.
- Em telas estreitas (≤ 1100px) os dois painéis empilham verticalmente.

---

## Escopo

Alterar dois arquivos:
- `frontend/src/app/App.tsx`
- `frontend/src/styles/globals.css`

---

## Requisitos

### 1. `frontend/src/app/App.tsx`

#### Mudança: substituir `aside.sidebar` por `div.panels-row`

Localizar o bloco (linhas 105–124):

```tsx
<aside className="sidebar">
  <Scoreboard
    connectionStatus={connectionStatus}
    tempo={gameState.tempo}
    playerCount={gameState.players.length}
    ball={gameState.ball}
    ballCarrierId={ballCarrier?.id ?? null}
  />
  <DebugPanel
    connectionStatus={connectionStatus}
    gameState={gameState}
    lastRawMessage={lastRawMessage}
    events={events}
    httpStatus={httpState.status}
    httpPlayersCount={httpState.players ? Object.keys(httpState.players).length : null}
    httpError={httpState.error}
    wsUrl={env.wsUrl}
    apiUrl={env.apiUrl}
  />
</aside>
```

Substituir por (`aside` → `div`, `sidebar` → `panels-row`):

```tsx
<div className="panels-row">
  <Scoreboard
    connectionStatus={connectionStatus}
    tempo={gameState.tempo}
    playerCount={gameState.players.length}
    ball={gameState.ball}
    ballCarrierId={ballCarrier?.id ?? null}
  />
  <DebugPanel
    connectionStatus={connectionStatus}
    gameState={gameState}
    lastRawMessage={lastRawMessage}
    events={events}
    httpStatus={httpState.status}
    httpPlayersCount={httpState.players ? Object.keys(httpState.players).length : null}
    httpError={httpState.error}
    wsUrl={env.wsUrl}
    apiUrl={env.apiUrl}
  />
</div>
```

Apenas a tag e a classe mudam — os filhos e props são idênticos.

### 2. `frontend/src/styles/globals.css`

#### 2.1. Alterar `.dashboard` para coluna única

Localizar (linhas 180–185):

```css
.dashboard {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.95fr);
  gap: 20px;
  align-items: start;
}
```

Substituir por:

```css
.dashboard {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}
```

#### 2.2. Substituir `.sidebar` por `.panels-row`

Localizar (linhas 187–190):

```css
.sidebar {
  display: grid;
  gap: 20px;
}
```

Substituir por (dois painéis lado a lado, com altura alinhada ao topo):

```css
.panels-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
  align-items: start;
}
```

#### 2.3. Ajustar altura mínima do `.viewport-panel`

Localizar (linhas 201–206):

```css
.viewport-panel {
  min-height: 720px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
```

Substituir por (com campo em coluna única a tela já dá mais espaço vertical — 
pode-se reduzir o mínimo para evitar scroll desnecessário em telas menores):

```css
.viewport-panel {
  min-height: 580px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
```

#### 2.4. Atualizar breakpoint `max-width: 1100px`

Localizar o bloco de media query (linhas 479–499):

```css
@media (max-width: 1100px) {
  #root {
    padding: 18px;
  }

  .dashboard {
    grid-template-columns: 1fr;
  }

  .viewport-panel {
    min-height: 580px;
  }

  .game-viewport {
    min-height: 460px;
  }

  .match-state-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
```

Substituir por (`.dashboard` já é 1 coluna; colapsar `.panels-row` para 1 coluna):

```css
@media (max-width: 1100px) {
  #root {
    padding: 18px;
  }

  .panels-row {
    grid-template-columns: 1fr;
  }

  .viewport-panel {
    min-height: 520px;
  }

  .game-viewport {
    min-height: 420px;
  }

  .match-state-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
```

> A regra `.dashboard { grid-template-columns: 1fr; }` que existia no breakpoint
> pode ser removida — `.dashboard` já é 1 coluna por padrão.

---

## Fora de Escopo

- Alterar os componentes internos `Scoreboard`, `DebugPanel`, `MatchStatePanel` ou `GameViewport`.
- Alterar o `coordinateMapper` ou a camada de renderização Pixi.
- Alterar `DEFAULT_FIELD_DIMENSIONS` (Task 2).
- Adicionar representação visual do gol (traves, rede).

---

## Validação

```powershell
cd frontend
npm run lint
npm run build
npm run dev
```

### Checklist visual (tela larga, ≥ 1440px)

- [ ] O card do campo ("Campo da partida") ocupa 100% da largura disponível
- [ ] O card "Estado da Jogada" e o card "Diagnóstico de payload" aparecem lado a lado abaixo do campo
- [ ] Os dois painéis têm larguras iguais (50%/50%)
- [ ] O campo não está distorcido — proporção 2:1 mantida
- [ ] Scroll não é necessário para ver o campo inteiro em tela cheia

### Checklist visual (tela estreita, ≤ 1100px)

- [ ] O card do campo ocupa 100% da largura (sem mudança)
- [ ] Os dois painéis empilham verticalmente (um sobre o outro)

### Checklist de código

- [ ] `npm run lint` passa sem erros
- [ ] `npm run build` passa sem erros
- [ ] Nenhuma referência ao seletor `.sidebar` resta em `App.tsx` ou no CSS

---

## Critérios de Sucesso

- `npm run lint` e `npm run build` passam.
- Campo ocupa 100% da largura superior.
- Painéis ficam lado a lado abaixo do campo em tela larga.
- Painéis empilham em tela estreita.
- Nenhuma regressão visual nos componentes internos.

---

## Resultado

_(preencher após execução)_
