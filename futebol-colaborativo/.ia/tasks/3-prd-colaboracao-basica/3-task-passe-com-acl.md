# Task 5: Passe com Comunicação ACL

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** Task 4 (posicionamento por zona)

---

## Problema

Aliados nunca cooperam. O jogador com a bola só pode chutar ou conduzir sozinho.
Não existe passe. Quando o zagueiro tem a bola e o atacante está adiantado no campo
ofensivo, nenhum dos dois tem mecanismo para trocar a bola.

---

## Objetivo

Implementar passe entre aliados com dois fluxos de iniciação:

1. **Receptor pede**: atacante envia `REQUEST` ao zagueiro; zagueiro decide aceitar ou recusar.
2. **Passador decide**: zagueiro detecta atacante próximo e envia `INFORM`; atacante confirma.

Em ambos os casos, a bola se move fisicamente no campo (sem teletransporte).
O passador mira na **posição atual do receptor no momento do chute**.
O receptor continua se movendo durante a negociação — o passe pode errar (realismo intencional).

---

## Escopo

Alterar `TipoDecisao.java`, `JogadorEstado.java`, `SistemaFutebol.java`,
`ContextoDecisao.java`, `JogadorAgent.java` e `ControladorDecisaoJogador.java`.

---

## Requisitos

### TipoDecisao.java

Adicionar:

```java
PASSAR_BOLA
```

### JogadorEstado.java

Adicionar campo serializado pelo Gson (visível no frontend para debug):

```java
public boolean aguardandoPasse;
```

### SistemaFutebol.java

Adicionar método:

```java
public Optional<String> localizarAliadoEmPosicaoDePasse(
        String nomeJogador, JogadorEstado estado, Time time) {
    // retorna o nome do aliado que está:
    // - no mesmo time
    // - dentro de RAIO_PASSE
    // - sem penalidade
    // - sem cooldown de passe
}
```

### ContextoDecisao.java

Adicionar campo e getter:

```java
private final String aliadoEmPosicaoDePasse; // null se nenhum disponível

public String getAliadoEmPosicaoDePasse() {
    return aliadoEmPosicaoDePasse;
}
```

O `JogadorAgent` passa o resultado de `localizarAliadoEmPosicaoDePasse` ao construir
o contexto.

### JogadorAgent.java

#### Novas constantes

```java
private static final double RAIO_PASSE = 20.0;
private static final double FORCA_PASSE = 1.0;
private static final int TICKS_COOLDOWN_PASSE = 10;
static final String CONVERSA_ID_PASSE = "passe-bola";
```

#### Novos campos de estado

```java
private boolean passePendente = false;      // agente está executando um passe
private boolean aguardandoPasse = false;    // agente pediu ou recebeu INFORM de passe
private int ticksCooldownPasse = 0;
```

#### Novo CyclicBehaviour para mensagens de passe

Registrar no `setup()` um segundo `CyclicBehaviour` que filtra por
`conversationId = CONVERSA_ID_PASSE` e chama `processarMensagemPasse(mensagem)`.

#### processarMensagemPasse(ACLMessage)

```java
private void processarMensagemPasse(ACLMessage mensagem) {
    switch (mensagem.getPerformative()) {
        case ACLMessage.REQUEST -> receberPedidoPasse(mensagem);   // atacante pediu
        case ACLMessage.INFORM  -> receberAvisoPasse(mensagem);    // zagueiro vai passar
        case ACLMessage.AGREE   -> receberConfirmacaoPasse(mensagem);
        case ACLMessage.REFUSE  -> limparEstadoPasse();
    }
}
```

#### receberPedidoPasse(ACLMessage) — lado do Zagueiro

O zagueiro recebe REQUEST do atacante:
- Se `passePendente == true`: ignorar (já está passando).
- Se `!estado.comBola`: recusar com REFUSE.
- Se `aliadoEmPosicaoDePasse != null`: AGREE e chamar `executarPasse(receptor)`.
- Caso contrário: REFUSE.

#### receberAvisoPasse(ACLMessage) — lado do Atacante

O atacante recebe INFORM do zagueiro:
- Setar `aguardandoPasse = true` e `estado.aguardandoPasse = true`.
- Responder com AGREE.
- O atacante continua seu ciclo normal de movimento — não para de se mover.

#### receberConfirmacaoPasse(ACLMessage) — lado do Zagueiro

O zagueiro recebeu AGREE do atacante após enviar INFORM:
- Chamar `executarPasse(receptor)`.

#### executarPasse(String nomeReceptor)

```java
private void executarPasse(String nomeReceptor) {
    JogadorEstado receptor = sistema.getEstado(nomeReceptor);
    if (receptor == null) {
        passePendente = false;
        return;
    }

    // mira na posição ATUAL do receptor no momento do chute
    double dx = receptor.x - estado.x;
    double dy = receptor.y - estado.y;
    double distancia = Math.sqrt(dx * dx + dy * dy);

    if (distancia == 0) distancia = 1;

    double forcaX = (dx / distancia) * FORCA_PASSE;
    double forcaY = (dy / distancia) * FORCA_PASSE;

    estado.comBola = false;
    passePendente = false;

    ACLMessage chute = new ACLMessage(ACLMessage.INFORM);
    chute.addReceiver(new AID("bola", AID.ISLOCALNAME));
    chute.setContent(forcaX + "," + forcaY);
    send(chute);

    if (sistema != null) {
        sistema.atualizarEstado(getLocalName(), estado);
    }

    System.out.printf("[%s] passe para %s | forca=(%.2f, %.2f)%n",
            getLocalName(), nomeReceptor, forcaX, forcaY);
}
```

#### Atacante solicita passe quando aliado tem a bola

No `executarDecisao`, quando a decisão for `INTERCEPTAR` e o jogador com a bola for
aliado (mesmo time), o atacante pode enviar REQUEST em vez de interceptar:

```java
case INTERCEPTAR:
    JogadorEstado jComBola = contexto.getJogadorComBola();
    if (jComBola != null && time.name().equals(jComBola.time)
            && !aguardandoPasse && ticksCooldownPasse == 0) {
        solicitarPasse(jComBola);
    } else if (jComBola != null) {
        agirSemBola(jComBola);
    } else {
        manterPosicaoDefensiva();
    }
    break;
```

#### solicitarPasse(JogadorEstado aliado)

```java
private void solicitarPasse(JogadorEstado aliado) {
    aguardandoPasse = true;
    estado.aguardandoPasse = true;

    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
    request.addReceiver(new AID(aliado.nome, AID.ISLOCALNAME)); // JogadorEstado precisa expor nome
    request.setConversationId(CONVERSA_ID_PASSE);
    request.setContent("tipo=passe;solicitante=" + getLocalName());
    send(request);

    System.out.printf("[%s] solicitando passe de %s%n", getLocalName(), aliado.nome);
}
```

> **Nota:** `JogadorEstado` já tem o campo `nome` implicitamente via chave no mapa do
> `SistemaFutebol`. Verificar se é necessário adicionar `public String nome` ao DTO
> ou usar outra forma de identificação.

#### Cooldown após receber passe

Quando o atacante ganha posse da bola após um passe (detectado em `perseguirBola` ou
`aplicarResultadoDisputa`), decrementar `ticksCooldownPasse` a cada tick até zerar.

Setar `ticksCooldownPasse = TICKS_COOLDOWN_PASSE` quando a bola chega e `aguardandoPasse == true`.
Limpar `aguardandoPasse` e `estado.aguardandoPasse` ao mesmo tempo.

### ControladorDecisaoJogador.java

Quando `eu_com_bola` e `aliadoEmPosicaoDePasse != null`:

```java
if (papel == PapelJogador.ATACANTE) {
    return TipoDecisao.AGIR_COM_BOLA;
}

return selecionarComPesos(Map.of(
    TipoDecisao.PASSAR_BOLA, 70,
    TipoDecisao.AGIR_COM_BOLA, 30
));
```

Somente o atacante envia `REQUEST`. O zagueiro nunca pede a bola; ele apenas
responde aos pedidos do atacante ou inicia um passe por decisao propria.

Quando `eu_com_bola` e nenhum aliado disponível: retornar `AGIR_COM_BOLA` (comportamento atual).

---

## Fora de Escopo

- Predição de trajetória do receptor (futura melhoria de perfil).
- Passe com mais de dois jogadores (ex: triangulação).
- Alterar `BolaAgent`.
- Alterar o protocolo de disputa (`"disputa-bola"`).

---

## Validação

```powershell
cd futebol-colaborativo
mvn test
mvn exec:java
```

Em outro terminal:

```powershell
cd frontend
npm run dev
```

### Checklist visual

- [ ] Log mostra `solicitando passe de ...` quando atacante pede a bola
- [ ] Log mostra `passe para ... | forca=(...)` quando o passe é executado
- [ ] A bola se move fisicamente de um jogador para o outro (não teletransporta)
- [ ] Atacante continua se movendo enquanto aguarda o passe
- [ ] Ocasionalmente o passe erra porque o receptor se moveu (comportamento esperado)
- [ ] Nenhuma disputa entre aliados (regressão do PRD 2)
- [ ] Gols continuam sendo registrados

---

## Critérios de Sucesso

- Código compila.
- Testes passam.
- Passes ocorrem visivelmente na simulação.
- A bola nunca teletransporta.
- O protocolo `"disputa-bola"` não é afetado.

---

## Resultado

- `TipoDecisao` passou a incluir `PASSAR_BOLA`; o zagueiro sorteia passe contra
  acao com bola nos pesos 70/30. O atacante nunca escolhe `PASSAR_BOLA`, e o
  zagueiro nunca envia solicitacoes de passe.
- `JogadorEstado` passou a expor `nome`, `aguardandoPasse` e `ticksCooldownPasse`,
  permitindo identificar o dono da bola e filtrar receptores em cooldown.
- `SistemaFutebol` agora localiza o aliado mais proximo, independentemente de estar
  a frente ou atras, no mesmo time, dentro do raio de 20 unidades, sem penalidade
  e sem cooldown.
- `JogadorAgent` recebeu um `CyclicBehaviour` exclusivo para `conversationId =
  "passe-bola"`, separado do protocolo `"disputa-bola"`.
- Implementados os dois fluxos ACL: atacante solicita com `REQUEST`, e o passador
  pode propor com `INFORM`; confirmacoes usam `AGREE` e indisponibilidade usa
  `REFUSE`.
- O passe usa a posicao atual do receptor para calcular o vetor e envia a forca ao
  `BolaAgent`; nao ha teletransporte.
- A intensidade do passe e fixa (`FORCA_PASSE = 1.0`), independentemente da
  distancia. O raio limita apenas quais receptores podem participar da jogada.
- O receptor continua se posicionando enquanto aguarda. Ao obter posse, entra em
  cooldown por 10 ticks. Negociacoes e esperas possuem timeout para evitar estado
  preso quando uma mensagem nao recebe resposta ou o passe erra.
- Ao ganhar a bola, o portador abre uma janela OODA de 2 ticks com a decisao
  `AGUARDAR_SOLICITACAO_PASSE`. Nesse intervalo ele mantem a posse e processa
  pedidos ACL antes de voltar a decidir entre passe e chute.
- Recusa, cancelamento ou timeout inicia um backoff de 5 ticks no solicitante,
  impedindo que o mesmo pedido seja repetido a cada ciclo de 100 ms.
- Se a posse mudar antes de um `REQUEST` ser processado, a resposta ACL continua
  sendo `REFUSE`, mas o estado publicado usa `CANCELADO` e o motivo
  `POSSE_ALTERADA_ANTES_DA_RESPOSTA`, diferenciando corrida de uma recusa de regra.
- A liberacao da bola antes de passes e chutes passou a ser sincronizada no
  `SistemaFutebol`, limpando atomicamente `JogadorEstado.comBola` e
  `Ambiente.bola.emPosseDe`. A forca fisica continua sendo enviada ao `BolaAgent`
  por ACL.
- Adicionados testes do controlador e de selecao de aliado no `SistemaFutebol`.
- O backend passou a publicar `passe` no snapshot WebSocket, com ID, passador,
  receptor, iniciador, status, forca, recebimento e resultado.
- Recusas agora carregam `motivoRecusa` estruturado, permitindo ao frontend
  distinguir passador sem bola, receptor fora do raio, penalidade, cooldown,
  jogador inexistente ou time incorreto.
- O frontend ganhou paineis de `Ultimo passe` abaixo do campo e no diagnostico,
  seguindo a mesma apresentacao visual de `Ultima disputa`.
- Validacao automatizada: `mvn clean test` com `BUILD SUCCESS`, 30 testes, zero
  falhas e zero erros.
- Frontend validado com `npm run lint` e `npm run build`.
- Smoke test manual do backend ficou pendente porque ja havia uma instancia Java
  ocupando as portas 1099, 8080 e 9090. A validacao visual permanece para a Task 6.
