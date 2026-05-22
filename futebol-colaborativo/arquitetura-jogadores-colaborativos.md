# Arquitetura para Jogadores Colaborativos

Este documento resume uma proposta incremental para evoluir a simulacao de futebol com JADE para um modelo mais colaborativo, inicialmente com:

- 2 jogadores azuis;
- 2 jogadores vermelhos;
- em cada time, 1 atacante e 1 zagueiro.

A ideia principal e preparar a arquitetura para decisoes por papel e, no futuro, decisoes probabilisticas simples, mantendo a comunicacao entre agentes por mensagens ACL. O ciclo de decisao dos jogadores pode ser pensado como uma adaptacao do ciclo OODA: Observar, Orientar, Decidir e Agir.

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

## 2. Uso de heranca com cuidado

Uma ideia natural seria usar heranca:

```text
JogadorAgent
  JogadorAtacanteAgent
  JogadorZagueiroAgent
```

Essa ideia pode ser boa, desde que a heranca seja usada para **especializar a configuracao inicial** do jogador, e nao para concentrar toda a inteligencia tatica.

Regra recomendada:

```text
O que todo jogador faz
  fica em JogadorAgent

O que muda por especialidade inicial
  fica em JogadorAtacanteAgent, JogadorZagueiroAgent etc.

O que muda durante o jogo
  fica em ContextoDecisao, PerfilTatico e ControladorDecisaoJogador
```

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

Ou seja, atacante e zagueiro podem existir como subclasses, mas elas devem ser **subclasses finas**.

Exemplo de responsabilidade das subclasses:

```text
JogadorAtacanteAgent extends JogadorAgent
  cria PerfilTatico ofensivo inicial
  define PapelJogador.ATACANTE

JogadorZagueiroAgent extends JogadorAgent
  cria PerfilTatico defensivo inicial
  define PapelJogador.ZAGUEIRO
```

No futuro, um `GoleiroAgent` tambem poderia existir. Ele faria sentido porque pode ter uma regra realmente especial, como pegar a bola com a mao. Mesmo assim, a decisao tatica continuaria passando pelo controlador.

---

## 3. Ideia arquitetural

A proposta fica hibrida:

- `JogadorAgent` e a base comum;
- `JogadorAtacanteAgent` e `JogadorZagueiroAgent` configuram papeis iniciais;
- o controlador de decisao continua fora das subclasses;
- as mensagens entre agentes continuam sendo ACL/JADE.

```text
JogadorAgent
  recebe mensagens ACL
  executa tick
  controla disputa
  monta ContextoDecisao
  chama ControladorDecisaoJogador
  executa acao

JogadorAtacanteAgent extends JogadorAgent
  cria PerfilTatico ofensivo inicial

JogadorZagueiroAgent extends JogadorAgent
  cria PerfilTatico defensivo inicial
```

O `JogadorAgent` continua responsavel por:

- rodar o `TickerBehaviour`;
- receber mensagens ACL;
- participar da disputa de bola;
- montar o contexto do momento;
- chamar o controlador de decisao;
- executar a acao concreta;
- atualizar o estado no sistema.

O controlador de decisao passa a cuidar de:

- entender o contexto;
- considerar o papel do jogador;
- escolher uma intencao;
- no futuro, aplicar probabilidades.

Exemplo conceitual de extensao para goleiro:

```java
public class GoleiroAgent extends JogadorAgent {

    @Override
    protected PerfilTatico criarPerfilTatico() {
        return PerfilTatico.goleiro();
    }

    @Override
    protected PapelJogador getPapel() {
        return PapelJogador.GOLEIRO;
    }

    @Override
    protected boolean podePegarComMao() {
        return true;
    }
}
```

O ponto importante: o goleiro pode ter uma capacidade especial, mas ainda participa do mesmo ciclo de percepcao, decisao, acao e comunicacao.

### 3.1 Ciclo OODA como modelo mental

O ciclo de cada jogador pode ser entendido como um OODA simplificado:

```text
Observar
  receber mensagens ACL
  ler estado da bola
  perceber companheiros e adversarios

Orientar
  montar ContextoDecisao
  considerar time, papel e PerfilTatico
  interpretar riscos, oportunidades e mensagens recebidas

Decidir
  chamar ControladorDecisaoJogador
  escolher uma TipoDecisao
  aplicar probabilidades no futuro

Agir
  mover, interceptar, chutar, passar ou defender
  enviar mensagens ACL se precisar colaborar
  atualizar estado no SistemaFutebol
```

Esse modelo ajuda a manter o `JogadorAgent` organizado: ele observa o ambiente, orienta sua percepcao, decide com autonomia e age no campo. No proximo tick, o resultado da acao volta como novo feedback para o ciclo.

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

Esse fluxo descreve o que aconteceria a cada tick de um jogador:

```text
JogadorAgent
  recebe mensagens ACL
  monta ContextoDecisao
  chama ControladorDecisaoJogador
  recebe uma TipoDecisao
  executa a acao concreta
  envia mensagens ACL se precisar colaborar
```

### 5.1 Recebe mensagens ACL

O agente verifica se outros agentes mandaram mensagens para ele. Hoje isso ja existe para disputa de bola. No futuro, tambem pode existir para mensagens taticas.

Exemplos:

```text
"estou livre para receber passe"
"voce esta marcado"
"cubra minha posicao"
"quero disputar a bola"
"aceito receber passe"
```

No JADE, a leitura continua acontecendo com `ACLMessage`, por exemplo:

```java
ACLMessage mensagem = myAgent.receive(mensagemTemplate);
```

### 5.2 Monta `ContextoDecisao`

Depois de ler mensagens, o jogador monta um resumo do momento atual. O contexto nao decide nada; ele so junta informacoes uteis.

Pode conter:

```text
meu nome
meu time
meu papel
minha posicao
estou com bola?
quem esta com a bola?
bola esta livre?
distancia ate o gol adversario
distancia ate meu proprio gol
tem adversario perto?
tem companheiro livre?
recebi pedido de passe?
recebi pedido de cobertura?
```

Exemplo:

```text
ContextoDecisao:
  jogador = azul-zagueiro
  papel = ZAGUEIRO
  comBola = true
  atacanteAliadoMarcado = true
  adversarioPerto = false
  distanciaGolAdversario = 35
```

### 5.3 Chama `ControladorDecisaoJogador`

O `JogadorAgent` entrega o contexto para uma classe especializada em decidir:

```java
TipoDecisao decisao = controlador.decidir(contexto);
```

O controlador pode considerar perguntas como:

```text
Estou com bola?
Sou atacante, zagueiro ou outro papel?
Meu companheiro esta livre?
Tem adversario perto?
Estou perto do gol?
Meu perfil prefere atacar ou defender?
Recebi alguma mensagem importante?
```

Ele usa o `PerfilTatico`.

```text
Zagueiro base:
  DEFENDER = 80%
  ATACAR   = 20%

Zagueiro com bola e atacante marcado:
  DEFENDER = 40%
  ATACAR   = 50%
  APOIAR   = 10%
```

### 5.4 Recebe uma `TipoDecisao`

O controlador nao move o jogador diretamente. Ele devolve uma intencao:

```text
PERSEGUIR_BOLA
INTERCEPTAR
DEFENDER
ATACAR
CHUTAR
PASSAR
APOIAR
RECUAR
MANTER_POSICAO
PEGAR_COM_MAO
```

Isso separa duas responsabilidades:

```text
Decidir o que fazer
Executar o que foi decidido
```

### 5.5 Executa a acao concreta

Depois da decisao, o agente transforma a intencao em acao real.

```text
TipoDecisao.PERSEGUIR_BOLA
  -> Movimento.mover(estado, bola.x, bola.y)

TipoDecisao.INTERCEPTAR
  -> CalculadoraInterceptacao calcula ponto
  -> Movimento.mover(estado, ponto.x, ponto.y)

TipoDecisao.CHUTAR
  -> cria ACLMessage para BolaAgent
  -> envia forca do chute

TipoDecisao.DEFENDER
  -> move para zona defensiva

TipoDecisao.PASSAR
  -> calcula direcao do companheiro
  -> envia passe/chute para BolaAgent

TipoDecisao.PEGAR_COM_MAO
  -> apenas GoleiroAgent ou jogador com permissao executa
```

### 5.6 Envia mensagens ACL se precisar colaborar

Depois de agir, o jogador pode avisar outros agentes.

```text
zagueiro -> atacante:
  "vou conduzir, se aproxime"

atacante -> zagueiro:
  "estou marcado, avance"

jogador com bola -> companheiro:
  "vou passar para voce"

zagueiro -> atacante:
  "cubra minha posicao"
```

Exemplo completo:

```text
1. azul-zagueiro recebe mensagem ACL:
   atacante azul mandou "estou marcado"

2. azul-zagueiro monta ContextoDecisao:
   sou zagueiro
   estou com bola
   atacante aliado esta marcado
   adversario nao esta perto
   tenho espaco para avancar

3. chama ControladorDecisaoJogador

4. controlador avalia:
   zagueiro normalmente defenderia
   mas atacante esta marcado
   entao aumenta chance de atacar

5. retorna TipoDecisao.ATACAR

6. JogadorAgent executa:
   conduz bola em direcao ao gol adversario

7. envia mensagem ACL:
   para atacante: "avance para receber passe depois"
```

---

## 6. Classes sugeridas

Uma divisao possivel, sem precisar criar tudo de uma vez:

```text
agentes/
  JogadorAgent              # agente JADE do jogador; executa ticks, mensagens e acoes
  JogadorAtacanteAgent      # especializacao fina; define papel e perfil ofensivo inicial
  JogadorZagueiroAgent      # especializacao fina; define papel e perfil defensivo inicial
  GoleiroAgent              # futuro; pode ter regra especial, como pegar com a mao
  BolaAgent                 # agente JADE da bola; recebe chutes e aplica fisica

jogo/
  Time                      # identifica o time do jogador, como AZUL ou VERMELHO
  PapelJogador              # identifica o papel inicial, como ATACANTE, ZAGUEIRO, GOLEIRO
  ConfiguracaoJogador       # guarda dados fixos: nome, time, papel, posicao inicial e gol
  ContextoDecisao           # junta informacoes atuais usadas para decidir uma acao
  TipoDecisao               # enum das intencoes: ATACAR, DEFENDER, PASSAR, CHUTAR etc.

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

2. Criar 4 jogadores.
   Primeiro pode ser tudo com `JogadorAgent`. Depois, criar subclasses finas: `JogadorAtacanteAgent` e `JogadorZagueiroAgent`.

3. Impedir disputa entre jogadores do mesmo time.
   Hoje o sistema pode tratar qualquer jogador proximo como oponente.

4. Criar `PerfilTatico`.
   No inicio, guardar apenas pesos base como defesa/ataque/apoio.

5. Criar `ContextoDecisao`.
   Deve concentrar informacoes como: quem tem bola, estou com bola, adversario esta perto, companheiro esta marcado, posicao no campo.

6. Extrair a decisao principal do `JogadorAgent`.
   O agente passa a chamar algo como `controladorDecisao.decidir(contexto)`.

7. Criar subclasses finas.
   `JogadorAtacanteAgent` e `JogadorZagueiroAgent` devem configurar perfil inicial, nao duplicar o ciclo JADE.

8. Extrair calculo de interceptacao.
   Mover `calcularPontoIntercepcao` para uma classe propria.

9. Extrair disputa de bola depois.
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

A decisao atual e usar uma arquitetura hibrida:

- heranca para especialidades iniciais;
- composicao/estrategia para decisao dinamica;
- mensagens ACL para colaboracao entre agentes.

Melhor caminho conceitual:

```text
JogadorAgent
  comportamento comum de qualquer jogador
  recebe mensagens ACL
  monta ContextoDecisao
  chama ControladorDecisaoJogador
  executa a acao concreta

JogadorAtacanteAgent extends JogadorAgent
  perfil inicial ofensivo

JogadorZagueiroAgent extends JogadorAgent
  perfil inicial defensivo

GoleiroAgent extends JogadorAgent
  futuro
  perfil inicial de goleiro
  capacidade especial, como pegar com a mao
```

Assim o projeto continua simples no presente, mas fica preparado para:

- passe;
- cobertura;
- marcacao;
- tomada de decisao por probabilidade;
- comportamento diferente por papel sem duplicar o ciclo JADE;
- novas especialidades futuras sem transformar cada classe em um bloco gigante de `if/else`.
