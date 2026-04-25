# agent.md - Template Padrao (Reutilizavel)

Documento vivo de onboarding para agentes e equipe do projeto **Futebol-Cooperativo-SMA / frontend**.

Este arquivo existe para reduzir contexto implicito. A expectativa e que qualquer pessoa (ou agente) consiga entender rapidamente:

- o que o sistema faz
- como executar localmente
- quais sao os fluxos principais
- quais contratos publicos existem
- quais decisoes arquiteturais foram tomadas
- quais pontos causam erro recorrente

Quando faltar informacao, registrar explicitamente:

> `Estado atual: nao existe/nao identificado no repositorio`

---

## 1. Visao geral do produto

**Descricao do sistema:**

> Este frontend renderiza uma partida de futebol cooperativo em tempo real usando React para UI e PixiJS para o campo 2D. O estado do jogo e alimentado por WebSocket, normalizado para um formato interno estavel e exibido tanto no renderer quanto em paineis React de estado da jogada e debug. A arquitetura suporta hoje o payload legado antigo, o payload legado novo com `jogadores + bola`, e continua preparada para um snapshot futuro mais estruturado.

**Objetivo funcional:**

- Exibir o estado autoritativo da partida em tempo real.
- Isolar transporte, normalizacao, estado e renderizacao para facilitar evolucao.
- Preparar a base do frontend para futuros endpoints HTTP, como iniciar partidas.

**Tecnologias principais (resumo):**

- Frontend: React 19 + TypeScript + Vite + PixiJS
- Backend: Java 17 + Maven + JADE + Spark Java + Java-WebSocket
- Banco: `Estado atual: nao existe/nao identificado no repositorio`

---

## 2. Stack e componentes

### Frontend

- React `19.2.4`
- TypeScript `~6.0.2`
- Vite `8.0.4`
- PixiJS `8.17.1`
- Gerenciamento de estado: estado local React com `useGameWebSocket`, sem store global externa
- Cliente HTTP: `fetch` nativo encapsulado em `src/services/http/httpClient.ts`

### Backend

- Linguagem: Java 17
- Framework principal: JADE para agentes
- API HTTP: Spark Java
- Persistencia: `Estado atual: nao existe/nao identificado no repositorio`
- Validacao: `Estado atual: nao existe/nao identificado no repositorio`
- Documentacao API: `Estado atual: nao existe/nao identificado no repositorio`
- Outros modulos relevantes: Gson para serializacao JSON e Java-WebSocket para stream de eventos

### Infraestrutura local

- Banco via Docker: `Estado atual: nao existe/nao identificado no repositorio`
- Volumes: `Estado atual: nao existe/nao identificado no repositorio`
- Servicos auxiliares: backend HTTP em `8080` e WebSocket em `9090`

---

## 3. Estrutura do repositorio

```text
Futebol-Cooperativo-SMA/
  README.md                                            # resumo curto do monorepo

  frontend/                                            # aplicacao web React + PixiJS
    agent.md                                           # onboarding tecnico do frontend e dos contratos atuais
    package.json                                       # scripts, dependencias e metadados do frontend
    package-lock.json                                  # lockfile do npm
    vite.config.ts                                     # configuracao do Vite
    tsconfig.json                                      # configuracao TypeScript raiz
    tsconfig.app.json                                  # configuracao TypeScript da aplicacao
    tsconfig.node.json                                 # configuracao TypeScript para tooling Node/Vite
    eslint.config.js                                   # regras de lint do frontend
    index.html                                         # HTML base servido pelo Vite
    README.md                                          # README do frontend
    public/                                            # arquivos publicos servidos sem bundle
      favicon.svg                                      # icone base do app no navegador
      icons.svg                                        # sprite SVG legado do template do Vite
    src/                                               # codigo-fonte principal do frontend
      main.tsx                                         # entrypoint React que monta a aplicacao
      app/                                             # composicao principal da tela
        App.tsx                                        # dashboard principal que integra viewport, estado e debug
      components/                                      # componentes visuais React
        game/                                          # ponte entre React e renderer Pixi
          GameViewport.tsx                             # monta/desmonta o canvas Pixi e injeta GameState
        panels/                                        # paineis auxiliares da interface
          Scoreboard.tsx                               # exibe estado da jogada, posse e bola
          DebugPanel.tsx                               # exibe payload bruto, eventos e bootstrap HTTP
      config/                                          # configuracao derivada do ambiente
        env.ts                                         # resolve URLs do backend com fallbacks locais
      game/                                            # dominio do jogo no frontend
        events/                                        # tipos e helpers para eventos efemeros
          gameEvents.ts                                # fila de eventos transitorios e fabrica de eventos
        mapping/                                       # transforma coordenadas do jogo para o canvas
          coordinateMapper.ts                          # mapeia grid do backend para viewport do Pixi
        model/                                         # tipos e normalizacao do estado interno
          gameTypes.ts                                 # contratos TypeScript do GameState canonico
          gameState.ts                                 # defaults e helpers para estado inicial e posse
          normalizers.ts                               # adapta payload legacy-flat, legacy-nested e snapshot
        renderer/                                      # camada isolada de renderizacao 2D com Pixi
          PixiGameApp.ts                               # inicializa Application, resize e fluxo de render
          FieldLayer.ts                                # desenha o campo e marcacoes
          PlayerLayer.ts                               # cria e atualiza entidades visuais dos jogadores
          BallLayer.ts                                 # renderiza a bola e destaca conducao/posse
      hooks/                                           # hooks React de integracao com o dominio
        useGameWebSocket.ts                            # conecta WebSocket, parseia mensagens e infere eventos
      services/                                        # acesso a transporte e integracoes
        api/                                           # fachada de endpoints do backend
          gameApi.ts                                   # expoe getStatus() e getPlayers()
        http/                                          # cliente HTTP generico
          httpClient.ts                                # wrapper de fetch com base URL e tratamento de erro
        websocket/                                     # transporte WebSocket desacoplado da UI
          gameSocket.ts                                # cliente WS com callbacks e reconexao automatica
      styles/                                          # estilos globais da aplicacao
        globals.css                                    # layout, tema e estilos do dashboard atual

  futebol-colaborativo/                                # backend principal do jogo multiagente
    pom.xml                                            # dependencias Maven e configuracao de execucao
    src/main/java/com/futebol/colaborativo/            # codigo Java principal do backend
      App.java                                         # bootstrap do sistema, API HTTP e WebSocket
      SistemaFutebol.java                              # orquestra jogadores e publica estado atualizado
      agentes/                                         # agentes JADE do jogo
        JogadorAgent.java                              # agente JADE que busca a bola e conduz ate o gol
      api/                                             # camada de transporte do backend
        ApiServer.java                                 # endpoints HTTP /api/jogadores e /api/status
        EventSocket.java                               # servidor WebSocket que faz broadcast dos estados
      model/                                           # modelos simples do dominio backend
        Ambiente.java                                  # dimensoes do campo, bola global e posicao do gol
        Bola.java                                      # modelo simples da bola
        JogadorEstado.java                             # DTO com posicao, velocidade e comBola
      movimento/                                       # regras de movimento utilitarias
        Movimento.java                                 # logica de movimento e conducao da bola

  escalacao-reputacao/                                 # outro projeto Java do monorepo, sem uso direto no frontend
    backend/
      pom.xml                                          # dependencias Maven desse subprojeto
      src/main/java/com/teamformation/
        MainContainer.java                             # bootstrap do experimento de formacao de times
        agents/
          CoachAgent.java                              # agente treinador do experimento paralelo
          PlayerAgent.java                             # agente jogador do experimento paralelo
          PlayerGui.java                               # interface grafica do experimento paralelo

  architecture/                                        # Estado atual: nao existe/nao identificado no repositorio
```

**Leitura da estrutura:**

- `frontend/` -> aplicacao React com WebSocket, normalizacao de payload, renderer Pixi e paineis de UI
- `futebol-colaborativo/` -> backend atual do jogo, com agentes JADE, endpoints HTTP e broadcast por WebSocket
- `escalacao-reputacao/` -> outro experimento/projeto Java no monorepo, sem integracao direta com o frontend atual
- `architecture/` -> `Estado atual: nao existe/nao identificado no repositorio`

---

## 4. Setup local

### Banco de dados

- Porta: `Estado atual: nao existe/nao identificado no repositorio`
- Database: `Estado atual: nao existe/nao identificado no repositorio`
- Usuario: `Estado atual: nao existe/nao identificado no repositorio`
- Senha: `Estado atual: nao existe/nao identificado no repositorio`

```bash
Estado atual: nao existe/nao identificado no repositorio
```

---

### Backend

**Variaveis de ambiente:**

- `Estado atual: nao existe/nao identificado no repositorio`

**Executar:**

```bash
cd futebol-colaborativo
mvn exec:java
```

**URL:**

- HTTP: `http://localhost:8080`
- WebSocket: `ws://localhost:9090`

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

**URL:**

- `http://localhost:5173`

**Observacoes importantes:**

- O frontend depende do backend rodando em `8080` e `9090`, a menos que `VITE_GAME_API_URL` e `VITE_GAME_WS_URL` sejam sobrescritas.
- O renderer Pixi nao abre conexao nem faz `fetch`; tudo passa pelas camadas `services/` e `hooks/`.

---

## 5. Arquitetura em alto nivel

### Resumo

- Fluxo principal: `WebSocket -> normalizers -> GameState interno -> Pixi + paineis React`
- React cuida da composicao da aplicacao e da UI declarativa.
- PixiJS cuida apenas da renderizacao do campo e das entidades visuais.
- O backend continua sendo a fonte autoritativa de verdade do jogo.

---

### Frontend

- O estado do jogo e mantido em memoria no hook `useGameWebSocket`.
- A conexao WebSocket fica em `src/services/websocket/gameSocket.ts`.
- A normalizacao fica em `src/game/model/normalizers.ts` e suporta `legacy-flat`, `legacy-nested` e `snapshot`.
- O renderer Pixi esta isolado em `src/game/renderer/`.
- A UI React principal fica em `src/app/App.tsx`, `src/components/panels/` e `src/components/game/GameViewport.tsx`.
- O frontend infere eventos leves de ciclo, como posse e provavel reset, apenas para debug.

**Consequencias:**

- O estado e efemero em memoria; um refresh perde o snapshot atual ate chegar nova mensagem.
- Payload invalido nao derruba a UI nem apaga o ultimo estado valido.
- O projeto continua single-page, sem router.
- Gol/reset mostrado no frontend e inferido, nao autoritativo.

---

### Backend

- Estrutura atual: `App` inicializa JADE, cria `SistemaFutebol`, sobe `ApiServer` e `EventSocket`.
- `SistemaFutebol` mantem os estados dos jogadores e transmite atualizacoes por WebSocket.
- `JogadorAgent` vai ate a bola, pega a bola, conduz ate o gol e reseta o ciclo.

---

### Integracoes externas

- `Estado atual: nao existe/nao identificado no repositorio`

---

## 6. Fluxos principais

### Fluxo 1 - Renderizacao em tempo real

1. O backend atualiza `JogadorEstado` em `SistemaFutebol`.
2. `SistemaFutebol` publica um payload WS com `jogadores` e `bola`.
3. `gameSocket.ts` recebe a mensagem crua via WebSocket.
4. `useGameWebSocket.ts` faz `JSON.parse`, chama `normalizeGamePayload` e infere eventos de posse/reset.
5. O `GameState` interno atualizado alimenta o `GameViewport` e os paineis React.

---

### Fluxo 2 - Bootstrap HTTP

1. `App.tsx` chama `gameApi.getStatus()` e `gameApi.getPlayers()` ao montar.
2. `httpClient.ts` centraliza `fetch` e tratamento de erro HTTP.
3. `DebugPanel.tsx` exibe o status retornado e a quantidade de jogadores no snapshot HTTP.

---

## 7. Frontend - rotas e comportamento

### Rotas publicas

- `/`

---

### Regras de navegacao

- O projeto nao usa React Router.
- Toda a experiencia atual cabe em uma unica tela SPA.

---

### Comportamentos importantes

- O canvas Pixi monta uma vez e recebe apenas o estado ja normalizado.
- O frontend aceita o payload legado antigo com jogadores no topo.
- O frontend aceita o payload legado novo com `jogadores` como mapa e `bola` separada.
- O frontend continua preparado para um snapshot futuro com `jogadores[]`, `placar` e `tempo`.
- Quando o socket cai, o estado visual anterior permanece enquanto a reconexao e tentada.

---

## 8. Backend - contratos publicos

### Endpoints principais

- `GET /api/jogadores`
- `GET /api/status`
- `WS ws://localhost:9090`

---

### Exemplo de payload

Payload WebSocket legado antigo:

```json
{
  "jogador1": {
    "x": 33,
    "y": 14,
    "velocidade": 1,
    "theta": 0
  }
}
```

Payload WebSocket atual:

```json
{
  "jogadores": {
    "jogador1": {
      "x": 99,
      "y": 30,
      "velocidade": 1,
      "comBola": true
    }
  },
  "bola": {
    "x": 99,
    "y": 30
  }
}
```

Payload WebSocket futuro esperado:

```json
{
  "tempo": 1250,
  "bola": { "x": 50.5, "y": 30.5, "emPosseDe": "jogador1" },
  "jogadores": [
    { "id": "jogador1", "time": "A", "x": 50, "y": 30 }
  ],
  "placar": { "A": 0, "B": 0 }
}
```

---

### Exemplo de resposta

Resposta atual de `GET /api/jogadores`:

```json
{
  "jogador1": {
    "x": 99,
    "y": 30,
    "velocidade": 1,
    "comBola": true
  }
}
```

Resposta atual de `GET /api/status`:

```json
"Sistema rodando com 1 jogadores"
```

---

### Erros comuns

```json
{
  "mensagem": "Estado atual: o backend nao define um envelope padronizado de erro HTTP/WS"
}
```

---

## 9. Regras de negocio

- O backend e a fonte autoritativa do estado do jogo.
- O frontend nao aplica regra de jogo; ele interpreta e desenha.
- O frontend nao calcula gol nem placar local.
- O payload legado antigo com chaves dinamicas e normalizado para `players[]`.
- O payload atual com `jogadores` como objeto e `bola` no topo e normalizado para `players[] + ball`.
- `comBola` define posse no jogador e pode gerar `ball.emPosseDe` por inferencia.
- Dados ausentes no snapshot atual caem em defaults:
  - `placar` vira `{ "A": 0, "B": 0 }`
  - `tempo` vira `null`
  - `bola` vira `null`
  - `team` legado vira `"unknown"`
  - `comBola` ausente vira `false`

---

## 10. Dados iniciais (seed)

- O backend atual cria automaticamente `jogador1` ao iniciar.
- O campo base usado no frontend e no backend e `100 x 60`.
- A bola inicia no centro do campo, em `50, 30`.
- A origem observada do jogador atual e `10, 10`.
- O placar no frontend continua em `0 x 0` quando o backend ainda nao envia esse dado.

---

## 11. Hurdles (armadilhas do projeto)

Liste problemas recorrentes:

1. Existem hoje dois formatos legados de WebSocket, e o frontend precisa suportar ambos sem confundir `jogadores`-objeto com `jogadores[]`.
2. O backend ainda nao envia placar, tempo nem evento formal de gol, entao qualquer indicacao de reset no frontend e apenas inferida.
3. O endpoint HTTP `/api/jogadores` ainda retorna apenas o mapa de jogadores, enquanto o WebSocket atual ja inclui `bola`.

---

## 12. Testes

### Frontend

- Validacoes atuais: `npm run lint` e `npm run build`
- Estado atual de testes automatizados: `nao existe/nao identificado no repositorio`

### Backend

- Configuracao: dependencias JUnit existem no `pom.xml`
- Banco em memoria: `Estado atual: nao existe/nao identificado no repositorio`
- Testes identificados no repositorio: `Estado atual: nao existe/nao identificado no repositorio`

---

## 13. Ausencias relevantes

Registrar explicitamente o que NAO existe:

- autenticacao: `Estado atual: nao existe/nao identificado no repositorio`
- observabilidade: `Estado atual: nao existe/nao identificado no repositorio`
- mensageria: `Estado atual: nao existe/nao identificado no repositorio`
- banco de dados: `Estado atual: nao existe/nao identificado no repositorio`
- docker-compose: `Estado atual: nao existe/nao identificado no repositorio`
- documentacao formal de API: `Estado atual: nao existe/nao identificado no repositorio`
- roteamento frontend: `Estado atual: nao existe/nao identificado no repositorio`
- evento formal de gol/reset no backend: `Estado atual: nao existe/nao identificado no repositorio`

---

## 14. Checklist de manutencao

Atualizar este documento quando mudar:

- rotas
- payload WebSocket
- endpoints HTTP
- defaults de normalizacao
- variaveis de ambiente
- infraestrutura local
- fluxos de sincronizacao entre backend e frontend

---

## 15. Ponto de partida para novos desenvolvedores/agentes

Ordem recomendada:

1. ler `agent.md`
2. ler `frontend/package.json`
3. revisar `src/app/App.tsx`
4. revisar `src/hooks/useGameWebSocket.ts`
5. revisar `src/game/model/normalizers.ts`
6. revisar `src/game/renderer/`
7. revisar `futebol-colaborativo/src/main/java/com/futebol/colaborativo/`

**Heuristica:**

- problema de UI -> `frontend/src/components` e `frontend/src/styles`
- problema de estado/transporte -> `frontend/src/hooks` e `frontend/src/services`
- problema de payload/contrato -> `frontend/src/game/model` e backend
- problema de renderizacao do campo -> `frontend/src/game/renderer`

---

## 16. Politica do documento

Este documento deve evoluir com o projeto.

Sempre registrar:

- novas regras
- mudancas de contrato
- decisoes arquiteturais
- problemas recorrentes
