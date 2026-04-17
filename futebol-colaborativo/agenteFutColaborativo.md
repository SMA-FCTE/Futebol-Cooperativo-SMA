# agenteFutColaborativo.md

Documento vivo de onboarding para o módulo `futebol-colaborativo`.

Este arquivo existe para reduzir contexto implícito do projeto e servir como ponto de partida para desenvolvimento, manutenção e atuação de agentes sobre o código. Ele descreve o que o módulo faz hoje, como executá-lo, quais componentes já existem e quais partes ainda estão ausentes ou incompletas.

Quando alguma informação ainda não existir no repositório, registrar explicitamente:

> Estado atual: não existe/não identificado no repositório

---

## 1. Visão geral do módulo

**Descrição do sistema:**

O `futebol-colaborativo` é um módulo Java orientado a sistemas multiagentes usando JADE. Hoje ele inicializa um contêiner principal do JADE, sobe um agente jogador de teste, mantém um estado compartilhado simples do ambiente de jogo, expõe leitura do estado por API HTTP e publica atualizações em tempo real via WebSocket.

O sistema representa uma simulação inicial de futebol cooperativo em grade, onde um agente persegue a bola, passa a conduzi-la ao gol quando a alcança e reinicia sua posição após marcar.

**Objetivo funcional atual:**

- iniciar uma simulação multiagente mínima com JADE
- modelar um jogador autônomo e um ambiente compartilhado
- disponibilizar o estado da simulação para consumo externo
- servir como base incremental para evolução de comportamentos colaborativos

**Estado de maturidade atual:**

- existe apenas um agente criado automaticamente no bootstrap: `jogador1`
- a simulação ainda é simplificada e centralizada em memória
- não há persistência de dados
- não há camada formal de coordenação entre múltiplos agentes além da infraestrutura JADE

---

## 2. Stack e componentes

### Base tecnológica

- Linguagem: Java 17
- Build: Maven
- Framework multiagente: JADE `4.5.0`
- API HTTP: Spark Java `2.9.4`
- Serialização JSON: Gson `2.10.1`
- Comunicação em tempo real: Java-WebSocket `1.5.4`
- Testes configurados: JUnit 5

### Componentes do módulo

- `App`:
  ponto de entrada da aplicação; sobe JADE, cria o sistema, inicializa um jogador, inicia API REST e WebSocket
- `SistemaFutebol`:
  orquestrador local do sistema; registra agentes, mantém estados dos jogadores e propaga atualizações para o WebSocket
- `JogadorAgent`:
  agente JADE com comportamento periódico; decide entre perseguir a bola ou conduzi-la ao gol
- `Ambiente`:
  estado global estático do campo, bola e posição do gol
- `Movimento`:
  regras de movimentação em grade e condução da bola
- `ApiServer`:
  endpoints HTTP de leitura de estado
- `EventSocket`:
  servidor WebSocket para broadcast do estado do jogo

### Infraestrutura local

- Banco de dados: Estado atual: não existe no repositório
- Docker Compose: Estado atual: não existe no repositório
- Serviços auxiliares externos obrigatórios: Estado atual: não identificados
- Interface JADE:
  a aplicação sobe o contêiner principal com `Profile.GUI = true`, então a GUI do JADE faz parte da execução local atual

---

## 3. Estrutura do repositório

```text
futebol-colaborativo/
|-- pom.xml
|-- APDescription.txt
|-- MTPs-Main-Container.txt
|-- src/
|   `-- main/
|       `-- java/
|           `-- com/futebol/colaborativo/
|               |-- App.java
|               |-- SistemaFutebol.java
|               |-- agentes/
|               |   `-- JogadorAgent.java
|               |-- api/
|               |   |-- ApiServer.java
|               |   `-- EventSocket.java
|               |-- model/
|               |   |-- Ambiente.java
|               |   |-- Bola.java
|               |   `-- JogadorEstado.java
|               `-- movimento/
|                   `-- Movimento.java
`-- target/
```

**Leitura da estrutura:**

- `src/main/java/com/futebol/colaborativo/`:
  núcleo da aplicação
- `agentes/`:
  agentes JADE e seus comportamentos
- `api/`:
  exposição externa do estado via HTTP e WebSocket
- `model/`:
  objetos simples de domínio e estado compartilhado
- `movimento/`:
  regras mecânicas de deslocamento
- `APDescription.txt` e `MTPs-Main-Container.txt`:
  arquivos auxiliares gerados/relacionados à execução do contêiner JADE
- `target/`:
  artefatos de build Maven

**Observação:**

- diretório `src/test`:
  Estado atual: não identificado durante a inspeção do módulo

---

## 4. Setup local

### Pré-requisitos

- Java 17
- Maven

### Executar validação básica

```bash
mvn test
```

Status verificado em `2026-04-17`: o comando executou com sucesso no ambiente local.

### Executar a aplicação

```bash
mvn exec:java
```

**Efeitos esperados ao subir:**

- inicializa o runtime JADE
- cria um main container com GUI habilitada
- instancia `SistemaFutebol`
- cria automaticamente o agente `jogador1`
- sobe a API REST na porta `8080`
- sobe o WebSocket na porta `9090`

### URLs locais

- API REST: `http://localhost:8080`
- WebSocket: `ws://localhost:9090`

### Observações importantes

- a aplicação depende do contêiner JADE já no bootstrap do processo
- o estado do sistema fica somente em memória
- o bootstrap atual cria somente um jogador de teste
- a GUI do JADE abre como parte do fluxo normal de execução

---

## 5. Arquitetura em alto nível

### Resumo

O módulo segue uma arquitetura simples em camadas leves:

- bootstrap da aplicação em `App`
- orquestração local em `SistemaFutebol`
- comportamento autônomo encapsulado em agentes JADE
- estado compartilhado simplificado em classes de modelo estáticas/em memória
- exposição do estado para consumidores externos via HTTP e WebSocket

### Arquitetura multiagente

- cada jogador é um agente JADE do tipo `JogadorAgent`
- o comportamento do jogador roda em um `TickerBehaviour` com intervalo de `500ms`
- a lógica atual do agente é reativa:
  se não está com a bola, persegue a bola; se está com a bola, conduz ao gol
- o estado do agente é reportado ao `SistemaFutebol` a cada ciclo

### Estado do ambiente

- `Ambiente.largura = 100`
- `Ambiente.altura = 60`
- `Ambiente.golX = largura`
- `Ambiente.golY = altura / 2`
- `Ambiente.bola` é global e compartilhada

### Consequências arquiteturais

- há acoplamento forte ao estado global estático do ambiente
- não existe isolamento entre simulação, transporte e domínio
- a simulação atual é determinística e simples
- como o estado fica em memória, reiniciar a aplicação perde todo o contexto

---

## 6. Fluxos principais

### Fluxo 1 — Inicialização do sistema

1. `App.main` inicia o runtime do JADE.
2. O main container é criado com GUI habilitada.
3. `SistemaFutebol` é instanciado com o `AgentContainer`.
4. O método `criarJogador("jogador1")` é chamado.
5. A API HTTP é iniciada na porta `8080`.
6. O servidor WebSocket é iniciado na porta `9090`.

### Fluxo 2 — Ciclo de decisão do jogador

1. O `JogadorAgent` inicia com posição padrão `(10, 10)`.
2. A cada `500ms`, o agente verifica se está com a bola.
3. Sem a bola:
   calcula direção até `Ambiente.bola` e move uma unidade no grid.
4. Com a bola:
   calcula direção até o gol e move conduzindo a bola junto.
5. O agente atualiza seu estado no `SistemaFutebol`.
6. O sistema serializa os estados e publica no WebSocket.

### Fluxo 3 — Marcação de gol

1. O jogador chega na coordenada do gol.
2. O sistema imprime mensagem de gol no console.
3. `comBola` volta para `false`.
4. A bola é reposicionada para o centro do campo.
5. O jogador é reposicionado novamente em `(10, 10)`.

---

## 7. Contratos públicos expostos

### API REST

#### `GET /api/jogadores`

Retorna o mapa de estados dos jogadores conhecido pelo `SistemaFutebol`.

**Formato esperado hoje:**

```json
{
  "jogador1": {
    "x": 10.0,
    "y": 10.0,
    "velocidade": 1.0,
    "comBola": false
  }
}
```

#### `GET /api/status`

Retorna um JSON serializado a partir de uma `String`, não de um objeto estruturado.

**Exemplo atual:**

```json
"Sistema rodando com 1 jogadores"
```

### WebSocket

#### `ws://localhost:9090`

O servidor faz broadcast do estado consolidado sempre que `SistemaFutebol.atualizarEstado(...)` é chamado.

**Formato de mensagem atual:**

```json
{
  "jogadores": {
    "jogador1": {
      "x": 10.0,
      "y": 10.0,
      "velocidade": 1.0,
      "comBola": false
    }
  },
  "bola": {
    "x": 50.0,
    "y": 30.0
  }
}
```

**Observações:**

- `onMessage` do WebSocket existe, mas hoje não processa comandos recebidos
- o socket é apenas de saída/broadcast no estado atual do projeto

---

## 8. Regras de negócio implementadas hoje

- o jogador anda em grid cardinal:
  `CIMA`, `BAIXO`, `DIREITA`, `ESQUERDA`
- a direção é escolhida pelo maior delta entre posição atual e alvo
- tocar na bola significa ter a mesma coordenada da bola
- ao tocar na bola, o jogador passa a carregá-la
- conduzir a bola move o jogador e sincroniza a posição da bola
- fazer gol significa alcançar exatamente a coordenada do gol
- após o gol, jogador e bola são reposicionados

**Limites e simplificações atuais:**

- não há colisão
- não há adversários
- não há passes
- não há posse compartilhada
- não há estratégia coletiva
- não há regras de impedimento, falta ou arbitragem
- não há limitação de borda do campo validada no movimento

---

## 9. Dados e estado inicial

### Jogador

- posição inicial: `x = 10`, `y = 10`
- velocidade padrão: `1.0`
- posse inicial da bola: `false`

### Ambiente

- largura: `100`
- altura: `60`
- gol: `(100, 30)`

### Bola

- objeto global de ambiente
- campos identificados: `x` e `y`
- posição inicial explícita no código fonte:
  Estado atual: não confirmada diretamente durante a inspeção textual do arquivo `Bola.java`
- inferência operacional:
  a bola é reposicionada para o centro do campo após gol em `(largura / 2, altura / 2)`

---

## 10. Hurdles do projeto

Liste aqui os problemas recorrentes ou pontos que merecem atenção ao evoluir o módulo:

1. O bootstrap cria apenas `jogador1`, então o sistema ainda não representa de fato uma dinâmica multiagente colaborativa completa.
2. O estado do ambiente é global e estático, o que pode dificultar testes, paralelismo e cenários com múltiplas partidas.
3. O endpoint `/api/status` devolve uma string JSON em vez de um objeto estruturado, o que reduz consistência da API.
4. O WebSocket não recebe comandos; hoje ele apenas transmite estado.
5. Não há persistência, então toda execução recomeça do zero.
6. A movimentação não valida limites do campo, o que pode gerar estados inválidos se a lógica crescer.
7. Foram observados sinais de possível sensibilidade a encoding em mensagens de console/string; vale manter atenção em acentuação ao evoluir o módulo.

---

## 11. Testes

### Situação atual

- dependências de JUnit 5 estão configuradas no `pom.xml`
- o plugin `maven-surefire-plugin` está configurado no build
- `mvn test` executou com sucesso em `2026-04-17`

### Lacunas

- casos de teste automatizados:
  Estado atual: não identificados no módulo durante esta inspeção
- testes de agentes JADE:
  Estado atual: não identificados
- testes de API:
  Estado atual: não identificados
- testes de integração WebSocket:
  Estado atual: não identificados

---

## 12. Ausências relevantes

Registrar explicitamente o que não existe hoje:

- autenticação:
  Estado atual: não existe
- autorização/perfis:
  Estado atual: não existe
- banco de dados:
  Estado atual: não existe
- persistência em disco:
  Estado atual: não existe
- observabilidade estruturada:
  Estado atual: não identificada além de logs em console
- mensageria externa:
  Estado atual: não existe
- criação dinâmica de jogadores por API:
  Estado atual: não existe
- comunicação semântica entre agentes:
  Estado atual: não identificada no código atual
- configuração externa por arquivo `.env` ou similar:
  Estado atual: não identificada
- documentação formal de arquitetura:
  Estado atual: este arquivo passa a cumprir esse papel inicial

---

## 13. Checklist de manutenção

Atualizar este documento quando mudar:

- quantidade e tipo de agentes
- forma de criação dos agentes
- comportamento dos agentes
- payloads da API REST
- eventos publicados por WebSocket
- regras do ambiente e do movimento
- portas e comandos de execução
- decisões arquiteturais relevantes

---

## 14. Ponto de partida para novos desenvolvedores/agentes

Ordem recomendada de leitura:

1. `pom.xml`
2. `src/main/java/com/futebol/colaborativo/App.java`
3. `src/main/java/com/futebol/colaborativo/SistemaFutebol.java`
4. `src/main/java/com/futebol/colaborativo/agentes/JogadorAgent.java`
5. `src/main/java/com/futebol/colaborativo/api/`
6. `src/main/java/com/futebol/colaborativo/model/`
7. este documento

**Heurística prática:**

- problema no bootstrap ou execução local:
  olhar `App.java`
- problema de criação/registro de agentes:
  olhar `SistemaFutebol.java`
- problema de comportamento autônomo:
  olhar `JogadorAgent.java`
- problema de movimento ou regras físicas:
  olhar `movimento/` e `model/`
- problema de integração externa:
  olhar `api/`

---

## 15. Política do documento

Este documento deve evoluir junto com o projeto.

Sempre registrar:

- novas regras de negócio
- mudanças nos contratos expostos
- decisões arquiteturais
- limitações conhecidas
- problemas recorrentes
- diferenças entre o objetivo do sistema e o que já está implementado

Se houver dúvida entre omitir ou registrar uma lacuna, preferir registrar.
