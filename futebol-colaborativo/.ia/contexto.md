# Contexto do Projeto Futebol Cooperativo SMA - Para Codex

> **Versao deste documento:** 0.5 - atualizada em 2026-06-26
> **Verdade-base:** o codigo do repositorio. Quando este documento e o codigo discordarem, o codigo vence e este documento deve ser atualizado.
>
> **Objetivo deste documento:** servir como fonte de contexto para agentes de IA trabalharem no projeto com consistencia, sem inventar arquitetura, padroes ou regras de negocio.
>
> **Idioma padrao do projeto:** Portugues brasileiro.

---

## 1. Visao Geral

**Futebol Cooperativo SMA** e um projeto academico de simulacao de futebol usando Sistemas Multiagentes. O backend Java usa JADE para criar agentes autonomos, manter o estado da partida e expor esse estado por HTTP/WebSocket. O frontend React renderiza o campo, jogadores, bola e informacoes de debug em tempo real.

O objetivo principal e evoluir uma simulacao inicial de jogadores autonomos para um modelo colaborativo, com jogadores de times diferentes, papeis taticos, comunicacao por mensagens ACL e tomada de decisao organizada via ciclo OODA.

### Contexto academico

- **Disciplina / contexto:** Sistemas Multiagentes / projeto academico.
- **Instituicao / organizacao:** UNB.
- **Responsaveis / equipe:** preencher depois.
- **Situacao atual:** prototipo funcional com colaboracao basica implementada.
- **Estado atual do desenvolvimento:** 4 PRDs concluidos; backend e frontend funcionais com agentes taticos, passe ACL, campo real e placar.

### Repositorio

- **Repositorio principal:** `Futebol-Cooperativo-SMA`
- **Branch principal:** `main`
- **Branch ativa de trabalho:** `nova-versao-vetor`
- **Modulos cobertos por este contexto:**
  - `futebol-colaborativo`
  - `frontend`

---

## 2. Dominio do Projeto

### 2.1. Problema que o projeto resolve

O projeto simula uma partida simplificada de futebol com agentes autonomos. Cada jogador percebe o ambiente, toma decisoes, age no campo e colabora com companheiros usando mensagens ACL.

O problema central e estudar/evoluir comportamento multiagente em um ambiente dinamico: disputa por bola, posse, movimentacao, ataque, defesa, interceptacao e colaboracao.

### 2.2. Objetivo do sistema

Construir uma simulacao de futebol cooperativo em que agentes de diferentes times consigam:

- movimentar-se no campo com posicionamento por zona tatica;
- disputar e recuperar a bola;
- atacar e defender com base no papel (atacante/zagueiro);
- comunicar intencoes por mensagens ACL (disputa e passe);
- chutar com direcao ao gol adversario;
- marcar gols e acumular placar.

### 2.3. Publico-alvo

- desenvolvedor do projeto;
- professor/orientador/avaliador;
- colegas que precisam entender a arquitetura;
- agentes de IA que vao auxiliar na evolucao do codigo.

### 2.4. Escopo atual

O escopo atual inclui:

- backend Java com JADE;
- agente de jogador (`JogadorAgent`) com ciclo OODA, roles e passe ACL;
- agente da bola (`BolaAgent`);
- 4 agentes: 2 por time (atacante + zagueiro cada);
- times AZUL e VERMELHO com papeis ATACANTE e ZAGUEIRO;
- campo 300×150 unidades;
- area de gol real (faixa de Y valida, nao apenas ponto central);
- chute dirigido ao gol com ruido angular;
- posicionamento por zona (atacante avancado, zagueiro recuado);
- passe colaborativo via mensagens ACL entre companheiros;
- placar por time exibido no frontend;
- estado compartilhado da partida;
- API HTTP simples;
- WebSocket para transmitir estado ao frontend;
- frontend React/Pixi para visualizar a simulacao com layout de campo completo.

Fora de escopo por enquanto:

- goleiro implementado;
- marcacao coletiva;
- tomada de decisao probabilistica;
- banco de dados ou persistencia;
- regras reais completas de futebol.

---

## 3. Stack Tecnologica

### 3.1. Backend `futebol-colaborativo`

| Item | Escolha atual |
| --- | --- |
| Linguagem principal | Java |
| Versao da linguagem | Java 17 |
| Build/dependencias | Maven |
| Agentes | JADE 4.5.0 |
| API HTTP | Spark Java 2.9.4 |
| JSON | Gson 2.10.1 |
| WebSocket | Java-WebSocket 1.5.4 |
| Testes | JUnit 5 configurado |

### 3.2. Frontend `frontend`

| Item | Escolha atual |
| --- | --- |
| Linguagem principal | TypeScript |
| UI | React |
| Build/dev server | Vite |
| Renderizacao do campo | Pixi.js |
| Lint | ESLint |
| Gerenciador de dependencias | npm |

### 3.3. Ferramentas de desenvolvimento

- IDE/editor: VS Code ou ambiente equivalente.
- Controle de versao: Git.
- Sistema operacional local observado: Windows.
- Shell observado: PowerShell.

---

## 4. Como Rodar Localmente

### 4.1. Backend

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

Efeitos esperados:

- sobe o container JADE;
- cria o sistema da partida;
- cria 4 agentes (2 azul: atacante-1/zagueiro-1, 2 vermelho: atacante-2/zagueiro-2);
- inicia API HTTP em `http://localhost:8080`;
- inicia WebSocket em `ws://localhost:9090`.

### 4.2. Frontend

```powershell
cd frontend
npm install
npm run dev
```

Comandos uteis:

```powershell
npm run build
npm run lint
npm run preview
```

Variaveis suportadas pelo frontend:

```text
VITE_GAME_API_URL   # fallback: http://localhost:8080
VITE_GAME_WS_URL    # fallback: ws://localhost:9090
```

---

## 5. Estrutura de Pastas Atual

```text
Futebol-Cooperativo-SMA/
|-- futebol-colaborativo/
|   |-- pom.xml
|   |-- .ia/
|   |   |-- contexto.md          <- este arquivo
|   |   |-- guia-desenvolvimento-com-ia.md
|   |   |-- tasks/
|   |       |-- 1-prd-refatoracao-arquitetura-jogador-ooda/
|   |       |   |-- prd.md
|   |       |   |-- techspec.md
|   |       |   |-- 1-task-...md  (tasks numeradas a partir de 1)
|   |       |-- 2-prd-dois-jogadores-por-time/
|   |       |-- 3-prd-colaboracao-basica/
|   |       |-- 4-prd-campo-real-area-gol-e-placar/
|   |-- src/main/java/com/futebol/colaborativo/
|       |-- App.java
|       |-- SistemaFutebol.java
|       |-- agentes/         (JogadorAgent, BolaAgent)
|       |-- api/             (ApiServer, EventSocket)
|       |-- dto/             (DisputaBolaEstadoDTO, PasseEstadoDTO)
|       |-- estrategia/      (ControladorDecisaoJogador, PerfilTatico, SeletorDecisaoPonderada)
|       |-- jogo/            (ConfiguracaoJogador, ContextoDecisao, PapelJogador, Time, TipoDecisao)
|       |-- model/           (Ambiente, Bola, JogadorEstado)
|       |-- movimento/       (Movimento)
|
|-- frontend/
|   |-- package.json
|   |-- vite.config.ts
|   |-- src/
|       |-- app/             (App.tsx)
|       |-- components/      (GameViewport, Scoreboard, DebugPanel, MatchStatePanel)
|       |-- config/
|       |-- game/            (model, events, rendering)
|       |-- hooks/
|       |-- services/
|       |-- styles/
```

### 5.1. Convencao de arquivos nas tasks

Cada pasta de PRD contem:

- `prd.md` — documento de requisitos do produto
- `techspec.md` — especificacao tecnica detalhada
- `1-task-<nome>.md` — primeira task de implementacao
- `2-task-<nome>.md` — segunda task, e assim por diante

### 5.2. Responsabilidade das principais pastas

| Pasta / arquivo | Responsabilidade |
| --- | --- |
| `agentes/` | Agentes JADE (`JogadorAgent`, `BolaAgent`) |
| `api/` | API HTTP e WebSocket |
| `dto/` | Objetos de transferencia ao frontend (disputa, passe) |
| `estrategia/` | Controlador de decisao, perfil tatico e seletor ponderado |
| `jogo/` | Enums e classes do dominio do jogo (Time, Papel, Contexto, Decisao, Configuracao) |
| `model/` | Estado do ambiente, bola e jogadores |
| `movimento/` | Funcoes de movimento e distancia |
| `frontend/src/game` | Modelo, normalizacao e renderizacao do jogo |
| `frontend/src/components` | Componentes visuais da interface |
| `frontend/src/services` | Cliente HTTP e WebSocket |
| `futebol-colaborativo/.ia` | Artefatos de processo com IA |

### 5.3. Arquivos criticos

| Arquivo | Por que e importante |
| --- | --- |
| `App.java` | Inicializa JADE, sistema, 4 agentes, API e WebSocket |
| `SistemaFutebol.java` | Orquestra estado, posse, gol, fisica, placar e broadcast |
| `JogadorAgent.java` | Ciclo OODA do jogador: decisao, movimento, disputa, passe, chute |
| `BolaAgent.java` | Recebe chutes e aciona fisica da bola |
| `ControladorDecisaoJogador.java` | Escolhe `TipoDecisao` com base no `ContextoDecisao` |
| `PerfilTatico.java` | Pesos por papel (atacante/zagueiro) para selecao de decisao |
| `ConfiguracaoJogador.java` | Dados fixos de cada jogador: nome, time, papel, posicao inicial, golX |
| `Movimento.java` | Centraliza mecanica de movimento |
| `frontend/src/app/App.tsx` | Tela principal que conecta HTTP/WebSocket e renderiza paineis |
| `frontend/src/game/model/gameTypes.ts` | Tipos principais do estado do jogo no frontend |
| `frontend/src/game/model/normalizers.ts` | Normaliza payloads do WebSocket em GameState |

---

## 6. Arquitetura Atual

### 6.1. Visao geral

```text
App.java
  -> cria SistemaFutebol
  -> cria BolaAgent e 4 JogadorAgent (com ConfiguracaoJogador por time/papel)
  -> inicia ApiServer
  -> inicia EventSocket

JogadorAgent (ciclo OODA)
  -> Observar: le bola, estado proprio, mensagens ACL
  -> Orientar: monta ContextoDecisao (time, papel, posse, zona)
  -> Decidir: ControladorDecisaoJogador escolhe TipoDecisao via PerfilTatico
  -> Agir: mover, chutar dirigido, passar (ACL), responder disputa (ACL)

BolaAgent
  -> recebe chute por ACLMessage
  -> chama SistemaFutebol.chutarBola

SistemaFutebol
  -> mantem estados de 4 jogadores
  -> aplica fisica da bola a cada tick
  -> detecta gol (faixa Y real), incrementa placar por time
  -> publica JSON no WebSocket com jogadores, bola, disputa, passe e placar

frontend
  -> busca bootstrap via HTTP
  -> acompanha jogo via WebSocket
  -> normaliza payload (formato legacy-nested)
  -> renderiza campo 300x150, jogadores com cor por time, bola, placar e debug
```

### 6.2. Componentes principais

| Componente | Responsabilidade |
| --- | --- |
| `App` | Bootstrap do backend |
| `SistemaFutebol` | Orquestrador local da partida |
| `JogadorAgent` | Agente JADE com ciclo OODA, roles, passe ACL e chute dirigido |
| `BolaAgent` | Agente JADE da bola |
| `ControladorDecisaoJogador` | Seleciona TipoDecisao com base em ContextoDecisao e PerfilTatico |
| `PerfilTatico` | Pesos de decisao por papel tatico |
| `SeletorDecisaoPonderada` | Sorteia decisao ponderada pelos pesos do perfil |
| `Ambiente` | Estado global do campo (300x150) e bola |
| `Bola` | Posicao, velocidade e posse da bola |
| `JogadorEstado` | Estado atual de um jogador |
| `Movimento` | Movimento, distancia e limites |
| `ApiServer` | Endpoints HTTP |
| `EventSocket` | Broadcast WebSocket |
| `frontend` | Visualizacao da simulacao |

### 6.3. Decisoes arquiteturais

| Decisao | Justificativa |
| --- | --- |
| Usar JADE | Projeto e baseado em Sistemas Multiagentes |
| Usar ACLMessage | Comunicacao entre agentes deve seguir o modelo JADE/FIPA |
| Manter estado em memoria | Simulacao academica/prototipo, sem persistencia por enquanto |
| Transmitir estado por WebSocket | Frontend precisa acompanhar simulacao em tempo real |
| Usar frontend separado | Facilita visualizacao e depuracao do estado |
| Um unico JogadorAgent com ConfiguracaoJogador | Papel e time sao configurados, nao subclasses; evita duplicacao do ciclo JADE |
| Ciclo OODA no tick do jogador | Combina com agentes autonomos em ambiente dinamico |
| Passe via ACL | Colaboracao entre companheiros segue o modelo multiagente do projeto |
| Placar em memoria no SistemaFutebol | Prototipo; reseta ao reiniciar |

---

## 7. Dominio e Entidades Principais

### 7.1. Entidades implementadas

| Entidade / classe | Responsabilidade |
| --- | --- |
| `Ambiente` | Define largura=300, altura=150, golYMin/golYMax e instancia global da bola |
| `Bola` | Controla posicao, velocidade e posse atual |
| `JogadorEstado` | Guarda x, y, velocidade, posse, penalidade, golX, time e papel |
| `Time` | Enum AZUL / VERMELHO |
| `PapelJogador` | Enum ATACANTE / ZAGUEIRO |
| `ConfiguracaoJogador` | Dados fixos por jogador (nome, time, papel, posicao inicial, golX) |
| `ContextoDecisao` | Snapshot situacional para o controlador de decisao |
| `TipoDecisao` | Enum de acoes possiveis (perseguir, chutar, passar, defender etc.) |
| `ControladorDecisaoJogador` | Escolhe TipoDecisao a partir do ContextoDecisao e PerfilTatico |
| `PerfilTatico` | Pesos de decisao por papel |
| `SeletorDecisaoPonderada` | Sorteia decisao com base nos pesos |
| `DisputaBolaEstadoDTO` | Estado da ultima disputa para exposicao ao frontend |
| `PasseEstadoDTO` | Estado do ultimo passe para exposicao ao frontend |
| `SistemaFutebol.OponenteDisputa` | Representa oponente proximo encontrado para disputa |

### 7.2. Estados importantes

| Estado | Significado |
| --- | --- |
| `estado.comBola` | Jogador possui a bola |
| `Ambiente.bola.emPosseDe` | Nome do jogador que possui a bola |
| `ticksPenalidadePerderDisputaRestantes` | Jogador perdeu disputa e fica temporariamente impedido |
| `disputaEmAndamento` | Jogador esta resolvendo disputa ACL |
| `ultimaDisputa` | Ultimo estado de disputa enviado ao frontend |
| `ultimoPasse` | Ultimo estado de passe enviado ao frontend |
| `golsTimeAzul` / `golsTimeVermelho` | Placar atual; resetado ao reiniciar o backend |

---

## 8. Regras de Negocio / Regras da Simulacao

### 8.1. Regras atuais

- O campo tem `largura=300` e `altura=150`.
- A bola inicia no centro do campo.
- O jogador se move em direcao a alvos usando `Movimento.mover`.
- Um jogador ganha posse ao tocar na bola (dentro do `RAIO_CONTATO_BOLA`).
- Quando um jogador com bola chuta, ele envia mensagem ACL para `BolaAgent`.
- `BolaAgent` interpreta a forca e o `SistemaFutebol` aplica a fisica.
- Chutes sao dirigidos ao centro do gol adversario com ruido angular aleatorio.
- A disputa de bola usa mensagens ACL com protocolo FIPA Contract Net (PEDRA/PAPEL/TESOURA).
- Quem perde disputa recebe penalidade temporaria.
- Disputa so ocorre entre adversarios (times diferentes).
- Atacante se posiciona na zona ofensiva; zagueiro, na zona defensiva.
- Passe: jogador envia REQUEST ACL ao aliado mais proximo dentro do raio de passe; aliado responde AGREE ou REFUSE.
- Gol ocorre quando `bola.x <= 0` ou `bola.x >= largura` E `bola.y ∈ [golYMin, golYMax]` (faixa real do gol).
- Gol no lado esquerdo (x=0): time VERMELHO marca. Gol no lado direito (x=largura): time AZUL marca.
- Ao gol, a bola volta ao centro, jogadores voltam as posicoes iniciais e o placar e atualizado.
- O payload WebSocket inclui: `jogadores`, `bola`, `disputa`, `passe`, `placar`.

### 8.2. Invariantes

- Apenas um jogador deve estar com `comBola = true` por vez.
- Quando a bola esta em posse, `Ambiente.bola.emPosseDe` deve indicar o jogador correto.
- Jogadores nao devem sair dos limites do campo apos movimento.
- O estado enviado ao frontend deve ser serializavel em JSON.
- Disputa ACL so e iniciada entre jogadores de times opostos.

### 8.3. Regras pendentes

- marcacao coletiva;
- regra de goleiro;
- decisao probabilistica com pesos dinamicos;
- persistencia de placar entre sessoes.

---

## 9. Comunicacao entre Componentes

### 9.1. Mecanismos atuais

- **ACL/JADE:** entre agentes (disputa e passe).
- **Chamadas Java diretas:** entre agentes e `SistemaFutebol`.
- **HTTP REST:** frontend busca status e jogadores.
- **WebSocket:** backend envia estado da simulacao ao frontend.

### 9.2. Mensagens/eventos atuais

| Mensagem / evento | Origem | Destino | Objetivo |
| --- | --- | --- | --- |
| `CFP` / `PROPOSE` / `ACCEPT_PROPOSAL` / `REJECT_PROPOSAL` / `REFUSE` | `JogadorAgent` | `JogadorAgent` | Resolver disputa de bola entre adversarios |
| `REQUEST` / `AGREE` / `REFUSE` | `JogadorAgent` | `JogadorAgent` | Negociar e executar passe entre aliados |
| `INFORM` com `forcaX,forcaY` | `JogadorAgent` | `BolaAgent` | Chutar a bola |
| `GET /api/jogadores` | Frontend | Backend | Obter estados dos jogadores |
| `GET /api/status` | Frontend | Backend | Obter status textual do sistema |
| Broadcast WebSocket | Backend | Frontend | Enviar estado consolidado da partida |

### 9.3. Comunicacao planejada

- mensagens taticas (avisos de posicionamento, pedido de cobertura).

---

## 10. Ciclo Principal de Execucao

### 10.1. Ciclo atual do jogador (OODA implementado)

```text
Tick do JogadorAgent
  |
  v
Observar
  le posicao da bola, estado proprio, mensagens ACL pendentes
  |
  v
Orientar
  monta ContextoDecisao (time, papel, posse, bolaNoFieldAdversario, aliados etc.)
  |
  v
Decidir
  ControladorDecisaoJogador.decidir(contexto, perfilTatico)
  retorna TipoDecisao
  |
  v
Agir
  PERSEGUIR_BOLA    -> mover em direcao a bola
  CHUTAR_GOL        -> chutar com angulo dirigido + ruido
  SOLICITAR_PASSE   -> enviar REQUEST ACL ao aliado
  POSICIONAR        -> mover para zona tatica (ataque ou defesa)
  DISPUTAR          -> iniciar/responder CFP ACL com adversario
  CONDUZIR          -> manter posse e mover
  ...
  |
  v
Atualiza estado no SistemaFutebol
```

### 10.2. Ciclo atual da bola

```text
Tick do BolaAgent
  |
  v
Ouve mensagens de chute (ACL INFORM)
  |
  v
SistemaFutebol aplica fisica (atrito, rebate, verificacao de gol)
  |
  v
SistemaFutebol publica estado no WebSocket (com placar)
```

### 10.3. Pontos sensiveis

- A disputa ACL ja funciona e deve ser alterada com cuidado.
- `Ambiente.bola` e estado global; facilita prototipo mas dificulta testes isolados.
- Frontend normaliza formatos legados; alterar payload pode quebrar visualizacao.
- Constantes de distancia/forca em `JogadorAgent` e `Movimento` estao calibradas para campo 300×150.

---

## 11. Padroes de Codigo

### 11.1. Convencoes gerais

- Preservar comportamento existente durante refatoracoes.
- Preferir mudancas pequenas e incrementais.
- Separar decisao de execucao quando a arquitetura permitir.
- Manter comunicacao entre agentes por ACL quando for interacao multiagente.
- Evitar colocar toda inteligencia no `SistemaFutebol`.

### 11.2. Convencoes de nomes

| Tipo | Convencao |
| --- | --- |
| Classes Java | `PascalCase` |
| Metodos Java | `camelCase` |
| Constantes Java | `UPPER_SNAKE_CASE` |
| Pacotes Java | minusculo |
| Tipos TypeScript | `PascalCase` |
| Funcoes/variaveis TypeScript | `camelCase` |

### 11.3. Comentarios

Comentarios devem explicar principalmente:

- decisoes nao obvias;
- regras de negocio;
- limitacoes conhecidas;
- motivo de uma abordagem.

Evitar comentarios que apenas repetem o que o codigo ja diz.

---

## 12. Testes e Validacao

### 12.1. Estado atual dos testes

- JUnit 5 configurado; 30 testes passando em `SistemaFutebolTest`.
- Frontend possui scripts de build e lint.

### 12.2. Validacao esperada

Backend:

```powershell
cd futebol-colaborativo
mvn test
```

Frontend:

```powershell
cd frontend
npm run lint
npm run build
```

Validacao manual:

- subir backend;
- subir frontend em `http://localhost:5173`;
- verificar campo 300x150 renderizado;
- verificar 4 jogadores (2 azul, 2 vermelho) e bola se movimentando;
- verificar placar `Azul 0 x 0 Vermelho` acima do canvas;
- verificar passes e disputas nos logs;
- verificar WebSocket conectado e painel de debug sem erro critico.

---

## 13. Funcionalidades Existentes

### 13.1. O que ja existe

- inicializacao do JADE com 4 agentes (2 por time);
- time e papel tatico (ATACANTE / ZAGUEIRO) por configuracao;
- ciclo de decisao OODA com `ContextoDecisao`, `ControladorDecisaoJogador` e `PerfilTatico`;
- movimento e perseguicao da bola;
- posicionamento por zona tatica (atacante avancado, zagueiro recuado);
- chute dirigido ao gol adversario com ruido angular;
- passe colaborativo por mensagens ACL entre companheiros;
- disputa de bola por mensagens ACL/FIPA (so entre adversarios);
- fisica simples da bola (atrito, rebate nas bordas);
- campo 300x150 com area de gol real (faixa Y);
- deteccao de gol com area real e contador de placar por time;
- API HTTP;
- WebSocket de estado com payload `jogadores, bola, disputa, passe, placar`;
- frontend com campo proporcional, jogadores com cor por time, placar e paineis de debug.

### 13.2. O que esta parcialmente implementado ou em teste

- comportamento ofensivo/defensivo baseado em zona e perfil, mas sem calibracao fina;
- seletor ponderado de decisao (implementado, pesos estaticos por papel);
- interceptacao (logica presente, nao e o foco do ciclo atual).

### 13.3. O que ainda nao existe

- goleiro;
- marcacao coletiva;
- decisao probabilistica com pesos dinamicos;
- persistencia de placar entre sessoes;
- regras reais completas de futebol.

---

## 14. Backlog / Proximas Mudancas

### 14.1. Possiveis proximas evolucoes

| ID | Mudanca | Prioridade | Observacao |
| --- | --- | --- | --- |
| EV-1 | Calibracao fina de constantes (forca, raios, pesos de decisao) | Media | A ser feita apos observacao visual da simulacao |
| EV-2 | Marcacao/cobertura entre companheiros | Media | Exige comunicacao tatica adicional por ACL |
| EV-3 | Goleiro | Baixa | Papel novo com logica proprio |
| EV-4 | Pesos de decisao dinamicos por situacao de jogo | Baixa | Extensao do PerfilTatico atual |

### 14.2. Debitos tecnicos conhecidos

- `JogadorAgent` ainda e o ponto central do comportamento; separar mais responsabilidades progressivamente.
- `Ambiente.bola` e estado global que dificulta testes isolados.
- Constante `MAX_DESVIO_ANGULO = Math.PI / 6.0` tem comentario incorreto ("120 graus"); valor real e 30 graus.

---

## 15. Decisoes Consolidadas

| Data | Decisao | Justificativa | Status |
| --- | --- | --- | --- |
| 2026-05-22 | Usar arquitetura hibrida para jogadores | Papel configurado em vez de subclasses; evita duplicar o ciclo JADE | Ativa |
| 2026-05-22 | Usar OODA como modelo mental do tick do jogador | Combina com agentes autonomos em ambiente dinamico | Implementado |
| 2026-05-22 | Manter mensagens ACL como base de colaboracao entre agentes | Projeto e de SMA com JADE | Ativa |
| 2026-06-26 | Campo 300x150 (era 100x60) | Mais espaco para manobras taticas e visibilidade | Ativa |
| 2026-06-26 | Area de gol real em vez de raio puntual | Coerencia visual entre frontend e backend | Ativa |
| 2026-06-26 | Placar em memoria no SistemaFutebol | Prototipo; persistencia fora de escopo por enquanto | Ativa |

---

## 16. Decisoes Pendentes

| Tema | Duvida | Impacto | Quem decide |
| --- | --- | --- | --- |
| Formato das mensagens taticas ACL | String simples, chave/valor ou JSON | Comunicacao tatica futura entre aliados | Equipe/projeto |
| Pesos de decisao | Quando e como tornar os pesos dinamicos | Comportamento dos agentes | Equipe/projeto |
| Goleiro | Quando criar e quais capacidades especiais | Escopo futuro | Equipe/projeto |

---

## 17. Riscos e Cuidados

### 17.1. Riscos tecnicos

- Quebrar a disputa de bola ao extrair logica ACL.
- Alterar payload do WebSocket sem ajustar o normalizer do frontend.
- Alterar constantes de distancia/forca sem recalibrar para campo 300x150.
- Transformar `SistemaFutebol` em controlador central de decisoes dos agentes.

### 17.2. Cuidados ao usar IA no projeto

Ao pedir mudancas para IA:

- ler este contexto primeiro;
- usar PRD/TechSpec/tasks quando a mudanca for grande;
- nao implementar arquitetura inteira de uma vez;
- nao aceitar alteracao ampla sem criterio de validacao;
- exigir resumo de arquivos alterados;
- preservar comunicacao multiagente por mensagens ACL;
- rodar validacoes possiveis depois de cada task.

---

## 18. Como a IA deve trabalhar neste projeto

### 18.1. Antes de alterar codigo

A IA deve:

1. Ler este `contexto.md`.
2. Identificar arquivos relacionados.
3. Explicar plano e riscos.
4. Aplicar mudancas pequenas.

### 18.2. Durante a implementacao

A IA deve:

- alterar o menor conjunto possivel de arquivos;
- preservar comportamento existente quando a task for refatoracao;
- nao introduzir dependencias sem justificativa;
- manter nomes e padroes do projeto;
- separar decisao de execucao quando possivel;
- evitar duplicacao de logica;
- tratar mensagens ACL como parte central do modelo multiagente.

### 18.3. Depois da implementacao

A IA deve informar:

- arquivos alterados;
- motivo de cada alteracao;
- como validar;
- riscos restantes;
- proximos passos recomendados.

---

## 19. Historico de Mudancas deste Documento

| Versao | Data | Mudanca |
| --- | --- | --- |
| 0.1 | 2026-05-22 | Criacao inicial do documento de contexto |
| 0.2 | 2026-06-26 | PRD 1: refatoracao OODA (ContextoDecisao, ControladorDecisaoJogador, PerfilTatico, TipoDecisao) |
| 0.3 | 2026-06-26 | PRD 2: 2 jogadores por time, Time, PapelJogador, ConfiguracaoJogador, disputa filtrada por time |
| 0.4 | 2026-06-26 | PRD 3: chute dirigido, posicionamento por zona, passe ACL |
| 0.5 | 2026-06-26 | PRD 4: campo 300x150, area de gol real, layout frontend, placar; renomeacao de arquivos .ia/tasks |

---

## 20. Contexto Especifico para Sistemas Multiagentes

### 20.1. Agentes existentes

| Agente | Responsabilidade |
| --- | --- |
| `JogadorAgent` | Jogador autonomo; ciclo OODA com decisao tatica, movimento, chute dirigido, passe ACL e disputa ACL |
| `BolaAgent` | Bola da partida; recebe chutes por ACL e dispara atualizacao fisica no SistemaFutebol |

Configuracao atual (4 instancias de JogadorAgent):

| Nome | Time | Papel | golX | Posicao inicial |
| --- | --- | --- | --- | --- |
| atacante-1 | AZUL | ATACANTE | 300 | (135, 75) |
| zagueiro-1 | AZUL | ZAGUEIRO | 300 | (60, 75) |
| atacante-2 | VERMELHO | ATACANTE | 0 | (165, 75) |
| zagueiro-2 | VERMELHO | ZAGUEIRO | 0 | (240, 75) |

### 20.2. Ambiente da simulacao

O ambiente e um campo 2D de 300×150 unidades. O gol ocupa uma faixa de Y em cada extremidade lateral (y ∈ [~59.88, ~90.12]). A posicao e detectada como gol quando a bola ultrapassa a linha lateral (x<=0 ou x>=300) dentro dessa faixa.

### 20.3. Percepcoes atuais dos agentes

Cada jogador consegue observar/consultar:

- posicao e velocidade da bola;
- proprio estado (posicao, posse, penalidade);
- configuracao propria (time, papel, golX);
- estado de posse conhecido pelo `SistemaFutebol`;
- oponente proximo para disputa;
- aliado disponivel para passe (dentro do raio de passe);
- mensagens ACL de disputa e passe.

### 20.4. Acoes atuais dos agentes

Cada jogador pode:

- mover para zona tatica (posicionamento);
- perseguir bola;
- conduzir bola;
- chutar com direcao ao gol adversario;
- solicitar passe a aliado (ACL REQUEST);
- receber e aceitar/recusar passe (ACL AGREE/REFUSE);
- iniciar/responder disputa por bola (ACL CFP);
- sofrer penalidade apos perder disputa.

### 20.5. Comunicacao entre agentes

Hoje existe comunicacao ACL para:

- disputa de bola entre adversarios (CFP/PROPOSE/ACCEPT/REJECT);
- passe entre aliados (REQUEST/AGREE/REFUSE);
- chute do jogador para a bola (INFORM).

### 20.6. Estrategia de decisao

Estado atual:

- ciclo OODA implementado no tick do `JogadorAgent`;
- `ContextoDecisao` monta o snapshot situacional;
- `ControladorDecisaoJogador` escolhe `TipoDecisao`;
- `PerfilTatico` define pesos por papel (atacante ofensivo, zagueiro defensivo);
- `SeletorDecisaoPonderada` sorteia a acao com base nos pesos.

### 20.7. Partes sensiveis da simulacao

Nao alterar sem cuidado:

- ciclo de `TickerBehaviour` do jogador e da bola;
- protocolo ACL da disputa (CFP com PEDRA/PAPEL/TESOURA);
- protocolo ACL do passe (REQUEST/AGREE/REFUSE);
- posse da bola e sincronizacao com `SistemaFutebol`;
- formato de payload usado pelo frontend (alterar exige atualizar `normalizers.ts`);
- constantes de distancia calibradas para campo 300x150.
