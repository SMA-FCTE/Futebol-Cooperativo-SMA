# Contexto do Projeto Futebol Cooperativo SMA - Para Codex

> **Versao deste documento:** 0.1 - atualizada em 2026-05-22
> **Verdade-base:** o codigo do repositorio. Quando este documento e o codigo discordarem, o codigo vence e este documento deve ser atualizado.
>
> **Objetivo deste documento:** servir como fonte de contexto para agentes de IA trabalharem no projeto com consistencia, sem inventar arquitetura, padroes ou regras de negocio.
>
> **Idioma padrao do projeto:** Portugues brasileiro.
>
> **Observacao importante:** este documento esta intencionalmente enxuto. Algumas secoes ficam pendentes porque a arquitetura dos jogadores esta em processo de evolucao.

---

## 1. Visao Geral

**Futebol Cooperativo SMA** e um projeto academico de simulacao de futebol usando Sistemas Multiagentes. O backend Java usa JADE para criar agentes autonomos, manter o estado da partida e expor esse estado por HTTP/WebSocket. O frontend React renderiza o campo, jogadores, bola e informacoes de debug em tempo real.

O objetivo principal e evoluir uma simulacao inicial de jogadores autonomos para um modelo mais colaborativo, com jogadores de times diferentes, papeis taticos, comunicacao por mensagens ACL e tomada de decisao mais organizada.

### Contexto academico

- **Disciplina / contexto:** Sistemas Multiagentes / projeto academico.
- **Instituicao / organizacao:** UNB.
- **Responsaveis / equipe:** preencher depois.
- **Situacao atual:** prototipo em evolucao arquitetural.
- **Estado atual do desenvolvimento:** backend e frontend funcionais; arquitetura de jogadores colaborativos em planejamento/refatoracao.

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

O projeto simula uma partida simplificada de futebol com agentes autonomos. Cada jogador deve perceber o ambiente, tomar decisoes, agir no campo e, no futuro, colaborar com companheiros usando mensagens.

O problema central e estudar/evoluir comportamento multiagente em um ambiente dinamico: disputa por bola, posse, movimentacao, ataque, defesa, interceptacao e colaboracao.

### 2.2. Objetivo do sistema

Construir uma simulacao de futebol cooperativo em que agentes de diferentes times consigam:

- movimentar-se no campo;
- disputar e recuperar a bola;
- atacar e defender;
- comunicar intencoes por mensagens ACL;
- evoluir para colaboracao como passe, cobertura e marcacao.

### 2.3. Publico-alvo

- desenvolvedor do projeto;
- professor/orientador/avaliador;
- colegas que precisam entender a arquitetura;
- agentes de IA que vao auxiliar na evolucao do codigo.

### 2.4. Escopo atual

O escopo atual inclui:

- backend Java com JADE;
- agente de jogador (`JogadorAgent`);
- agente da bola (`BolaAgent`);
- estado compartilhado da partida;
- API HTTP simples;
- WebSocket para transmitir estado ao frontend;
- frontend React/Pixi para visualizar a simulacao;
- documento de arquitetura dos jogadores colaborativos.

Fora de escopo por enquanto:

- goleiro implementado;
- passe colaborativo completo;
- marcacao coletiva;
- tomada de decisao probabilistica implementada;
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
- cria agentes iniciais;
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
|   |-- agenteFutColaborativo.md
|   |-- arquitetura-jogadores-colaborativos.md
|   |-- .ia/
|   |-- src/main/java/com/futebol/colaborativo/
|       |-- App.java
|       |-- SistemaFutebol.java
|       |-- agentes/
|       |-- api/
|       |-- dto/
|       |-- model/
|       |-- movimento/
|
|-- frontend/
|   |-- package.json
|   |-- vite.config.ts
|   |-- src/
|       |-- app/
|       |-- components/
|       |-- config/
|       |-- game/
|       |-- hooks/
|       |-- services/
```

### 5.1. Responsabilidade das principais pastas

| Pasta / arquivo | Responsabilidade |
| --- | --- |
| `futebol-colaborativo/src/main/java/com/futebol/colaborativo` | Backend Java da simulacao |
| `futebol-colaborativo/src/main/java/com/futebol/colaborativo/agentes` | Agentes JADE (`JogadorAgent`, `BolaAgent`) |
| `futebol-colaborativo/src/main/java/com/futebol/colaborativo/model` | Estado do ambiente, bola e jogadores |
| `futebol-colaborativo/src/main/java/com/futebol/colaborativo/movimento` | Funcoes de movimento e distancia |
| `futebol-colaborativo/src/main/java/com/futebol/colaborativo/api` | API HTTP e WebSocket |
| `frontend/src/game` | Modelo, normalizacao e renderizacao do jogo |
| `frontend/src/components` | Componentes visuais da interface |
| `frontend/src/services` | Cliente HTTP e WebSocket |
| `futebol-colaborativo/.ia` | Artefatos de processo com IA |

### 5.2. Arquivos criticos

| Arquivo | Por que e importante |
| --- | --- |
| `App.java` | Inicializa JADE, sistema, agentes, API e WebSocket |
| `SistemaFutebol.java` | Orquestra estado da partida, posse, gol, fisica da bola e broadcast |
| `JogadorAgent.java` | Contem o comportamento atual do jogador e a disputa ACL |
| `BolaAgent.java` | Recebe chutes e aciona fisica da bola |
| `Movimento.java` | Centraliza mecanica de movimento |
| `frontend/src/app/App.tsx` | Tela principal que conecta HTTP/WebSocket e renderiza paineis |
| `frontend/src/game/model/gameTypes.ts` | Tipos principais do estado do jogo no frontend |
| `arquitetura-jogadores-colaborativos.md` | Documento de decisao da arquitetura futura dos jogadores |

---

## 6. Arquitetura Atual

### 6.1. Visao geral

```text
App.java
  -> cria SistemaFutebol
  -> cria BolaAgent e JogadorAgent
  -> inicia ApiServer
  -> inicia EventSocket

JogadorAgent
  -> decide movimento/acao
  -> envia estado ao SistemaFutebol
  -> envia mensagens ACL para disputa e chute

BolaAgent
  -> recebe chute por ACLMessage
  -> chama SistemaFutebol.chutarBola

SistemaFutebol
  -> mantem estados
  -> atualiza Ambiente.bola
  -> publica JSON no WebSocket

frontend
  -> busca bootstrap via HTTP
  -> acompanha jogo via WebSocket
  -> renderiza campo, jogadores, bola e debug
```

### 6.2. Componentes principais

| Componente | Responsabilidade |
| --- | --- |
| `App` | Bootstrap do backend |
| `SistemaFutebol` | Orquestrador local da partida |
| `JogadorAgent` | Agente JADE do jogador; hoje concentra decisao, movimento, disputa e posse |
| `BolaAgent` | Agente JADE da bola |
| `Ambiente` | Estado global do campo e bola |
| `Bola` | Posicao, velocidade e posse da bola |
| `JogadorEstado` | Estado atual de um jogador |
| `Movimento` | Movimento, distancia e limites |
| `ApiServer` | Endpoints HTTP |
| `EventSocket` | Broadcast WebSocket |
| `frontend` | Visualizacao da simulacao |

### 6.3. Decisoes arquiteturais atuais

| Decisao | Justificativa |
| --- | --- |
| Usar JADE | Projeto e baseado em Sistemas Multiagentes |
| Usar ACLMessage | Comunicacao entre agentes deve seguir o modelo JADE/FIPA |
| Manter estado em memoria | Simulacao academica/prototipo, sem persistencia por enquanto |
| Transmitir estado por WebSocket | Frontend precisa acompanhar simulacao em tempo real |
| Usar frontend separado | Facilita visualizacao e depuracao do estado |

### 6.4. Arquitetura futura em planejamento

Detalhes estao no arquivo `futebol-colaborativo/arquitetura-jogadores-colaborativos.md`.

Resumo da direcao planejada:

```text
JogadorAgent
  comportamento comum e ciclo JADE

JogadorAtacanteAgent extends JogadorAgent
  perfil ofensivo inicial

JogadorZagueiroAgent extends JogadorAgent
  perfil defensivo inicial

ControladorDecisaoJogador
  decide com base em ContextoDecisao e PerfilTatico
```

Esta arquitetura ainda nao esta implementada no codigo atual.

---

## 7. Dominio e Entidades Principais

| Entidade / classe | Responsabilidade |
| --- | --- |
| `Ambiente` | Define largura, altura, gol e instancia global da bola |
| `Bola` | Controla posicao, velocidade e posse atual |
| `JogadorEstado` | Guarda `x`, `y`, velocidade, posse, penalidade e `golX` |
| `DisputaBolaEstadoDTO` | Estado da ultima disputa para exposicao ao frontend |
| `SistemaFutebol.OponenteDisputa` | Representa oponente proximo encontrado para disputa |

Estados importantes:

| Estado | Significado |
| --- | --- |
| `estado.comBola` | Jogador possui a bola |
| `Ambiente.bola.emPosseDe` | Nome do jogador que possui a bola |
| `ticksPenalidadePerderDisputaRestantes` | Jogador perdeu disputa e fica temporariamente impedido |
| `disputaEmAndamento` | Jogador esta resolvendo disputa ACL |
| `ultimaDisputa` | Ultimo estado de disputa enviado ao frontend |

Entidades planejadas, ainda nao consolidadas:

- `Time`
- `PapelJogador`
- `PerfilTatico`
- `ContextoDecisao`
- `TipoDecisao`
- `ControladorDecisaoJogador`

---

## 8. Regras de Negocio / Regras da Simulacao

### 8.1. Regras atuais

- O campo possui dimensoes globais em `Ambiente`.
- A bola inicia no centro do campo.
- O jogador se move em direcao a alvos usando `Movimento.mover`.
- Um jogador ganha posse ao tocar na bola.
- Quando um jogador com bola chuta, ele envia mensagem ACL para `BolaAgent`.
- `BolaAgent` interpreta a forca e o `SistemaFutebol` aplica a fisica.
- A disputa de bola usa mensagens ACL com protocolo FIPA Contract Net.
- A disputa atual usa jogadas `PEDRA`, `PAPEL`, `TESOURA`.
- Quem perde disputa recebe penalidade temporaria.
- Ao gol, a bola volta ao centro e jogadores voltam para suas posicoes iniciais.

### 8.2. Invariantes

- Apenas um jogador deve estar com `comBola = true` por vez.
- Quando a bola esta em posse, `Ambiente.bola.emPosseDe` deve indicar o jogador correto.
- Jogadores nao devem sair dos limites do campo apos movimento.
- O estado enviado ao frontend deve ser serializavel em JSON.

### 8.3. Regras planejadas

Preencher depois da refatoracao de jogadores colaborativos:

- regra de time;
- regra de papel tatico;
- regra de decisao probabilistica;
- regra de passe;
- regra de marcacao/cobertura.

---

## 9. Comunicacao entre Componentes

### 9.1. Mecanismos atuais

- **ACL/JADE:** entre agentes.
- **Chamadas Java diretas:** entre agentes e `SistemaFutebol`.
- **HTTP REST:** frontend busca status e jogadores.
- **WebSocket:** backend envia estado da simulacao ao frontend.

### 9.2. Mensagens/eventos atuais

| Mensagem / evento | Origem | Destino | Objetivo |
| --- | --- | --- | --- |
| `CFP` / `PROPOSE` / `ACCEPT_PROPOSAL` / `REJECT_PROPOSAL` / `REFUSE` | `JogadorAgent` | `JogadorAgent` | Resolver disputa de bola |
| `INFORM` com `forcaX,forcaY` | `JogadorAgent` | `BolaAgent` | Chutar a bola |
| `GET /api/jogadores` | Frontend | Backend | Obter estados dos jogadores |
| `GET /api/status` | Frontend | Backend | Obter status textual do sistema |
| Broadcast WebSocket | Backend | Frontend | Enviar estado consolidado da partida |

### 9.3. Comunicacao planejada

Preencher depois da arquitetura colaborativa. Exemplos previstos:

- pedido de passe;
- aviso de marcacao;
- pedido de cobertura;
- proposta/confirmacao de passe;
- mensagens taticas entre companheiros.

---

## 10. Ciclo Principal de Execucao

### 10.1. Ciclo atual do jogador

```text
Tick do JogadorAgent
  |
  v
Verifica penalidade
  |
  v
Verifica disputa em andamento
  |
  v
Tenta iniciar disputa se houver oponente proximo
  |
  v
Decide acao principal
  |
  |-- ninguem com bola --> perseguir bola
  |-- jogador com bola -> chutar/conduzir
  |-- outro com bola ---> interceptar
  |
  v
Atualiza estado no SistemaFutebol
```

### 10.2. Ciclo atual da bola

```text
Tick do BolaAgent
  |
  v
Ouve mensagens de chute
  |
  v
SistemaFutebol aplica fisica da bola
  |
  v
SistemaFutebol publica estado no WebSocket
```

### 10.3. Ciclo futuro planejado: OODA

O ciclo dos jogadores deve se aproximar de um modelo OODA:

```text
Observar
  receber ACL, ler bola, jogadores e ambiente

Orientar
  montar ContextoDecisao com time, papel, perfil e mensagens

Decidir
  ControladorDecisaoJogador escolhe TipoDecisao

Agir
  mover, chutar, passar, defender ou enviar ACL
```

Esta versao ainda esta em planejamento/documentacao.

### 10.4. Pontos sensiveis

- `JogadorAgent` mistura muitas responsabilidades.
- A disputa ACL ja funciona e deve ser refatorada com cuidado.
- `Ambiente.bola` e estado global facilitam prototipo, mas dificultam testes.
- `SistemaFutebol.localizarOponenteProximoParaDisputa` ainda nao considera time.
- Frontend normaliza formatos legados; alterar payload pode quebrar visualizacao.

---

## 11. Padroes de Codigo

### 11.1. Convenções gerais

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

- JUnit 5 esta configurado no backend.
- Nao ha garantia neste documento de cobertura automatizada relevante.
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
- subir frontend;
- verificar se o campo renderiza;
- verificar jogadores e bola se movimentando;
- verificar WebSocket conectado;
- verificar painel de debug sem erro critico.

---

## 13. Funcionalidades Existentes

### 13.1. O que ja existe

- inicializacao do JADE;
- criacao de jogadores e bola;
- movimento de jogador;
- perseguicao da bola;
- chute da bola;
- fisica simples da bola;
- disputa de bola por mensagens ACL/FIPA;
- API HTTP;
- WebSocket de estado;
- frontend de visualizacao.

### 13.2. O que esta parcialmente implementado ou em teste

- comportamento ofensivo/defensivo ainda simplificado;
- interceptacao;
- chute aleatorio de teste no `JogadorAgent`;
- suporte do frontend a formatos diferentes de payload.

### 13.3. O que ainda nao existe

- papeis taticos implementados como classes/estrategias;
- 2 jogadores por time de forma consolidada;
- passe colaborativo;
- comunicacao tatica entre companheiros;
- goleiro;
- decisao probabilistica;
- persistencia.

---

## 14. Backlog / Proximas Mudancas

### 14.1. Mudancas planejadas

| ID | Mudanca | Prioridade | Observacao |
| --- | --- | --- | --- |
| AJC-1 | Formalizar PRD/TechSpec/tasks da arquitetura de jogadores colaborativos | Alta | Artefatos em `.ia/tasks/prd-arquitetura-jogadores-colaborativos` |
| AJC-2 | Adicionar time e papel ao dominio | Alta | Base para 2x2 e evitar disputa entre companheiros |
| AJC-3 | Criar arquitetura de decisao com contexto/controlador/perfil | Alta | Preparar OODA e probabilidades |
| AJC-4 | Criar subclasses finas para atacante/zagueiro | Media | Heranca deve configurar perfil, nao duplicar ciclo JADE |
| AJC-5 | Extrair interceptacao e disputa gradualmente | Media | Partes sensiveis |

### 14.2. Debitos tecnicos conhecidos

- `JogadorAgent` concentra responsabilidades demais.
- Estado global em `Ambiente`.
- Falta modelagem explicita de time.
- Falta separacao clara entre decisao tatica e acao concreta.
- Possiveis problemas de encoding em textos com acentos ja apareceram em arquivos/documentos.

---

## 15. Decisoes Consolidadas

| Data | Decisao | Justificativa | Status |
| --- | --- | --- | --- |
| 2026-05-22 | Usar arquitetura hibrida para jogadores | Heranca para especialidade inicial e composicao/estrategia para decisao dinamica | Ativa |
| 2026-05-22 | Usar OODA como modelo mental do tick do jogador | Combina com agentes autonomos em ambiente dinamico | Ativa |
| 2026-05-22 | Manter mensagens ACL como base de colaboracao entre agentes | Projeto e de Sistemas Multiagentes com JADE | Ativa |

---

## 16. Decisoes Pendentes

| Tema | Duvida | Impacto | Quem decide |
| --- | --- | --- | --- |
| Estrutura exata dos pacotes novos | `jogo`, `estrategia`, `disputa`, `comunicacao` etc. | Organizacao da refatoracao | Equipe/projeto |
| Formato das mensagens taticas ACL | String simples, chave/valor, JSON ou outro formato | Comunicacao entre agentes | Equipe/projeto |
| Probabilidades | Quando e como aplicar pesos taticos | Comportamento dos agentes | Equipe/projeto |
| Goleiro | Quando criar e quais capacidades especiais | Escopo futuro | Equipe/projeto |

---

## 17. Riscos e Cuidados

### 17.1. Riscos tecnicos

- Quebrar a disputa de bola ao extrair logica ACL.
- Criar subclasses grandes demais e duplicar o ciclo JADE.
- Transformar `SistemaFutebol` em controlador central de decisoes.
- Alterar payload do WebSocket sem ajustar o frontend.
- Misturar refatoracao arquitetural com mudanca grande de comportamento.

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
2. Ler `arquitetura-jogadores-colaborativos.md` quando a mudanca envolver jogadores.
3. Identificar arquivos relacionados.
4. Explicar plano e riscos.
5. Aplicar mudancas pequenas.

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
| 0.1 | 2026-05-22 | Criacao inicial do documento de contexto para backend `futebol-colaborativo` e `frontend` |

---

## 20. Contexto Especifico para Sistemas Multiagentes

### 20.1. Agentes existentes

| Agente | Responsabilidade |
| --- | --- |
| `JogadorAgent` | Jogador autonomo; move, persegue bola, chuta, intercepta, disputa bola e atualiza estado |
| `BolaAgent` | Bola da partida; recebe chutes e dispara atualizacao fisica |

### 20.2. Ambiente da simulacao

O ambiente atual e um campo 2D simplificado com largura, altura, gol e bola global definidos em `Ambiente`. Os jogadores possuem posicao `(x, y)`, velocidade, posse e gol alvo.

### 20.3. Percepcoes atuais dos agentes

Cada jogador consegue observar/consultar:

- posicao da bola;
- proprio estado;
- estado de posse conhecido pelo `SistemaFutebol`;
- oponente proximo para disputa;
- mensagens ACL de disputa.

Percepcoes planejadas:

- companheiro livre/marcado;
- adversario em zona de perigo;
- pedidos de passe;
- pedidos de cobertura;
- contexto tatico por time/papel.

### 20.4. Acoes atuais dos agentes

Cada jogador pode:

- mover;
- perseguir bola;
- conduzir bola;
- chutar;
- tentar interceptar;
- iniciar/responder disputa;
- sofrer penalidade apos perder disputa.

Acoes planejadas:

- passar;
- pedir passe;
- cobrir posicao;
- marcar adversario;
- ajustar decisao por perfil tatico;
- goleiro pegar bola com a mao (futuro, nao implementado).

### 20.5. Comunicacao entre agentes

Hoje existe comunicacao ACL para:

- disputa de bola entre jogadores;
- chute do jogador para a bola.

No futuro, a colaboracao deve usar mensagens ACL tambem para comunicacao tatica entre companheiros.

### 20.6. Estrategia de decisao

Estado atual:

- decisao por condicionais dentro de `JogadorAgent`.

Direcao planejada:

- ciclo inspirado em OODA;
- `ContextoDecisao` para orientar a decisao;
- `ControladorDecisaoJogador` para escolher `TipoDecisao`;
- `PerfilTatico` para pesos iniciais;
- probabilidades depois que a arquitetura base estiver estavel.

### 20.7. Partes sensiveis da simulacao

Nao alterar sem cuidado:

- ciclo de `TickerBehaviour` do jogador;
- ciclo de `TickerBehaviour` da bola;
- protocolo ACL da disputa;
- posse da bola;
- atualizacao de estado no `SistemaFutebol`;
- formato de payload usado pelo frontend.
