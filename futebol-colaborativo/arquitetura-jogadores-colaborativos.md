# Arquitetura para Jogadores Colaborativos

Este documento resume uma proposta incremental para evoluir a simulacao de futebol com JADE para um modelo mais colaborativo, inicialmente com:

- 2 jogadores azuis;
- 2 jogadores vermelhos;
- em cada time, 1 atacante e 1 zagueiro.

A ideia principal e preparar a arquitetura para decisoes por papel e, no futuro, decisoes probabilisticas simples.

---

## 1. Problema atual

Hoje o `JogadorAgent` concentra muitas responsabilidades:

- ciclo de vida JADE;
- recebimento/envio de mensagens ACL;
- disputa de bola via FIPA Contract Net;
- movimentacao;
- perseguicao da bola;
- conducao ao gol;
- chute;
- calculo de interceptacao;
- posse de bola;
- penalidade apos perder disputa;
- atualizacao do estado no `SistemaFutebol`.

Isso funciona para uma simulacao pequena, mas dificulta criar comportamento coletivo. A classe mistura infraestrutura JADE, mecanica de jogo e decisao tatica.

---

## 2. Por que nao criar `JogadorAtacanteAgent` e `JogadorZagueiroAgent`

Uma ideia natural seria usar heranca:

```text
JogadorAgent
  JogadorAtacanteAgent
  JogadorZagueiroAgent
```

Mas isso pode ficar rigido cedo demais.

Um zagueiro nao deve ser um agente que "so defende". Ele pode defender na maior parte do tempo, mas tambem atacar dependendo do contexto.

Exemplo:

```text
Zagueiro base:
  DEFENDER = 80%
  ATACAR   = 20%

Se o atacante estiver marcado:
  DEFENDER = 50%
  ATACAR   = 40%
  APOIAR   = 10%
```

Ou seja, atacante e zagueiro sao melhores como **papeis taticos configuraveis**, nao como subclasses fixas.

---

## 3. Ideia arquitetural

Manter um unico `JogadorAgent` como agente JADE e delegar a decisao para componentes externos.

```text
JogadorAgent
  |
  v
ControladorDecisaoJogador
  |
  v
ContextoDecisao + PerfilTatico
  |
  v
DecisaoJogador
  |
  v
Acao concreta
```

O `JogadorAgent` continua responsavel por:

- rodar o `TickerBehaviour`;
- receber mensagens ACL;
- participar da disputa de bola;
- atualizar o estado no sistema.

O controlador de decisao passa a cuidar de:

- entender o contexto;
- considerar o papel do jogador;
- escolher uma intencao;
- no futuro, aplicar probabilidades.

---

## 4. Fluxo atual do jogador

```text
Tick do JogadorAgent
  |
  v
Esta em penalidade?
  |-- sim --> reduz penalidade e termina
  |
  v
Esta em disputa?
  |-- sim --> nao se move e termina
  |
  v
Existe oponente perto?
  |-- sim --> inicia disputa ACL/FIPA
  |
  v
Decide acao principal
  |
  |-- ninguem com bola --> persegue bola
  |-- eu com bola -------> conduz ou chuta
  |-- outro com bola ----> intercepta
```

No curto prazo, esse fluxo pode continuar igual. A primeira mudanca importante e substituir `decidirAcaoPrincipal()` por uma chamada a um controlador de decisao.

---

## 5. Fluxo futuro com papel tatico

Exemplo: zagueiro azul sem bola.

```text
Tick do JogadorAgent
  |
  v
Monta ContextoDecisao
  |
  v
Papel do jogador: ZAGUEIRO
  |
  v
PerfilTatico sugere mais defesa
  |
  v
Escolhe intencao
  |
  |-- DEFENDER --> voltar zona defensiva ou interceptar
  |-- ATACAR ----> avancar se houver oportunidade
  |-- APOIAR ----> aproximar para passe/cobertura
```

Exemplo: zagueiro com bola e atacante marcado.

```text
Zagueiro esta com bola
  |
  v
Atacante do mesmo time esta marcado?
  |
  v
Aumenta chance de decisao ofensiva
  |
  v
Zagueiro pode conduzir, chutar ou procurar passe
```

---

## 6. Classes sugeridas

Uma divisao possivel, sem precisar criar tudo de uma vez:

```text
agentes/
  JogadorAgent              # agente JADE do jogador; executa ticks, mensagens e acoes
  BolaAgent                 # agente JADE da bola; recebe chutes e aplica fisica

jogo/
  Time                      # identifica o time do jogador, como AZUL ou VERMELHO
  PapelJogador              # identifica o papel inicial, como ATACANTE ou ZAGUEIRO
  ConfiguracaoJogador       # guarda dados fixos: nome, time, papel, posicao inicial e gol
  ContextoDecisao           # junta informacoes atuais usadas para decidir uma acao
  TipoDecisao               # enum das intencoes possiveis: ATACAR, DEFENDER, APOIAR

estrategia/
  PerfilTatico              # pesos base do papel, como 80% defesa e 20% ataque
  ControladorDecisaoJogador # escolhe a decisao do jogador a partir do contexto
  SeletorDecisaoPonderada   # sorteia uma decisao usando pesos/probabilidades

disputa/
  JogadaDisputa             # enum das jogadas da disputa, como PEDRA, PAPEL, TESOURA
  ResultadoDisputa          # enum do resultado: vitoria local, vitoria oponente ou empate
  ProtocoloDisputaBola      # concentra a logica ACL/FIPA da disputa de bola

movimento/
  Movimento                 # funcoes mecanicas de mover, conduzir e calcular distancia
  CalculadoraInterceptacao  # calcula onde um jogador pode tentar interceptar outro
```

---

## 7. Refatoracao incremental

1. Adicionar `Time` e `PapelJogador`.
   Isso permite diferenciar azul/vermelho e atacante/zagueiro.

2. Criar 4 jogadores ainda usando `JogadorAgent`.
   Exemplo: `azul-atacante`, `azul-zagueiro`, `vermelho-atacante`, `vermelho-zagueiro`.

3. Impedir disputa entre jogadores do mesmo time.
   Hoje o sistema pode tratar qualquer jogador proximo como oponente.

4. Criar `PerfilTatico`.
   No inicio, guardar apenas pesos base como defesa/ataque/apoio.

5. Criar `ContextoDecisao`.
   Deve concentrar informacoes como: quem tem bola, estou com bola, adversario esta perto, companheiro esta marcado, posicao no campo.

6. Extrair a decisao principal do `JogadorAgent`.
   O agente passa a chamar algo como `controladorDecisao.decidir(contexto)`.

7. Extrair calculo de interceptacao.
   Mover `calcularPontoIntercepcao` para uma classe propria.

8. Extrair disputa de bola depois.
   A disputa ACL/FIPA ja funciona e e uma parte sensivel. Melhor mexer nela quando time, papel e decisao estiverem mais claros.

---

## 8. Fluxo de arquivos no sistema

```text
App.java
  cria SistemaFutebol
  cria BolaAgent
  cria jogadores

SistemaFutebol.java
  registra jogadores
  guarda estados
  define posse
  localiza oponentes
  publica estado no WebSocket

JogadorAgent.java
  executa tick
  processa mensagens ACL
  pede decisao ao controlador
  executa acao escolhida
  atualiza estado

BolaAgent.java
  recebe mensagens de chute
  aplica fisica da bola

Movimento.java
  move jogador
  conduz bola
  calcula distancia
```

---

## 9. Comunicacao entre agentes

Como o projeto usa Sistemas Multiagentes com JADE, a colaboracao entre jogadores deve acontecer principalmente por **mensagens ACL**. O `SistemaFutebol` pode guardar o estado global e ajudar com consultas, mas ele nao deve virar uma mente central que decide tudo por todos.

Cada `JogadorAgent` deve continuar autonomo:

- percebe o ambiente;
- recebe mensagens de outros agentes;
- atualiza seu contexto local;
- decide conforme seu papel e perfil tatico;
- executa uma acao;
- envia novas mensagens quando precisar coordenar.

Exemplos de mensagens futuras:

```text
Pedido de passe
  atacante -> zagueiro
  "estou livre, pode tocar"

Aviso de marcacao
  zagueiro -> atacante
  "adversario proximo, voce esta marcado"

Pedido de cobertura
  atacante -> zagueiro
  "vou avancar, cubra minha posicao"

Proposta de passe
  jogador com bola -> companheiro
  "posso tocar para voce?"
```

Fluxo multiagente desejado:

```text
JogadorAgent recebe mensagens ACL
  |
  v
Monta ou atualiza ContextoDecisao
  |
  v
ControladorDecisaoJogador escolhe uma intencao
  |
  v
Jogador executa a acao escolhida
  |
  v
Se necessario, envia novas mensagens ACL
```

Assim, a arquitetura continua sendo multiagente de verdade: os agentes conversam, negociam, informam, pedem apoio e tomam decisoes locais.

---

## 10. Resumo da decisao

Nao usar `JogadorAtacanteAgent` e `JogadorZagueiroAgent` agora porque isso transforma papeis flexiveis em classes fixas.

Melhor caminho:

```text
Um unico JogadorAgent
  + time
  + papel
  + perfil tatico
  + contexto de decisao
  + controlador de decisao
```

Assim o projeto continua simples no presente, mas fica preparado para:

- passe;
- cobertura;
- marcacao;
- tomada de decisao por probabilidade;
- comportamento diferente por papel sem duplicar o agente JADE.
