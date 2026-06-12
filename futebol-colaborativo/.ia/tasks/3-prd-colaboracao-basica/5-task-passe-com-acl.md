# Task 5: Passe com Comunicação ACL

> **Status:** pending
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`
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
2. **Passador decide**: zagueiro detecta atacante à frente e envia `INFORM`; atacante confirma.

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
    // - mais próximo de golX do que o passador (está à frente no ataque)
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
private static final double RAIO_PASSE = 30.0;
private static final int TICKS_VIAGEM_PASSE = 15;
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

    double forca = distancia / TICKS_VIAGEM_PASSE;
    double forcaX = (dx / distancia) * forca;
    double forcaY = (dy / distancia) * forca;

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
// pesos de chutar vs passar por papel
int pesoPasse  = papel == PapelJogador.ZAGUEIRO ? 70 : 30;
int pesoChute  = 100 - pesoPasse;
return selecionarComPesos(Map.of(
    TipoDecisao.PASSAR_BOLA,  pesoPasse,
    TipoDecisao.AGIR_COM_BOLA, pesoChute
));
```

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

_(preencher após execução)_
