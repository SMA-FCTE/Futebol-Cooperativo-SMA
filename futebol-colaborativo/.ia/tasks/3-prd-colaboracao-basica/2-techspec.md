# TechSpec: Comportamento Tático e Passe Colaborativo

> **PRD:** `1-prd.md`
> **Versão:** 0.2

---

## 1. Contexto técnico

### 1.1. Chute atual

`JogadorAgent.agirComBola()` delega para `chutarAleatorioParaTeste()`:

```java
double forcaX = (random.nextDouble() * 2.0 - 1.0) * FORCA_CHUTE_ALEATORIO_TESTE;
double forcaY = (random.nextDouble() * 2.0 - 1.0) * FORCA_CHUTE_ALEATORIO_TESTE;
```

Resultado: vetor uniforme em qualquer direção, sem referência ao `golX` do agente.

### 1.2. Decisão de bola livre — sem consciência de zona

`ControladorDecisaoJogador.decidir()` sorteia `PERSEGUIR_BOLA` ou
`MANTER_POSICAO_DEFENSIVA` com base nos pesos do `PerfilTatico`.
Não existe leitura da posição da bola relativa ao meio-campo nem
coordenação entre aliados: zagueiro e atacante do mesmo time podem
ambos ir para a mesma bola ao mesmo tempo.

### 1.3. Ausência de passe e de zona

`TipoDecisao` não tem `PASSAR_BOLA`. Não existe protocolo ACL de passe.
Não existe conceito de "campo próprio" vs "campo adversário" no controlador.

---

## 2. Task 3 — Chute Dirigido ao Gol

### 2.1. Abordagem

Calcular o ângulo em direção ao gol e aplicar ruído angular aleatório:

```text
anguloBase  = atan2(Ambiente.golY - estado.y, golX - estado.x)
ruido       = random U(-MAX_DESVIO_ANGULO, +MAX_DESVIO_ANGULO)
anguloFinal = anguloBase + ruido

forcaX = cos(anguloFinal) * FORCA_CHUTE_DIRIGIDO
forcaY = sin(anguloFinal) * FORCA_CHUTE_DIRIGIDO
```

### 2.2. Constantes

| Constante | Valor | Justificativa |
|-----------|-------|---------------|
| `MAX_DESVIO_ANGULO` | `Math.PI * 2.0 / 3.0` (~120°) | Maioria vai para o lado certo; desvios grandes ocasionais (inclusive para o campo próprio) para parecer humano |
| `FORCA_CHUTE_DIRIGIDO` | `1.35` | Mantém dinâmica atual |

### 2.3. Arquivos alterados

| Arquivo | Mudança |
|---------|---------|
| `JogadorAgent.java` | Criar `chutarDirigidoAoGol()`; substituir chamada em `agirComBola()`; remover `chutarAleatorioParaTeste()` e `FORCA_CHUTE_ALEATORIO_TESTE` |

---

## 3. Task 4 — Posicionamento por Zona

### 3.1. Conceito de zona

Cada jogador tem um "campo de responsabilidade" definido pelo papel.
A zona não é uma barreira física — é uma penalidade extra nos pesos do
`ControladorDecisaoJogador` quando a bola está no campo adversário.

**Referência de zona:**

```text
meioX = Ambiente.largura / 2   // linha do meio-campo

bolaNoFieldAdversário(bola, golX):
    se golX == 0:  bola.x < meioX    // time vermelho ataca para esquerda
    se golX == 100: bola.x > meioX   // time azul ataca para direita
```

Ou de forma unificada: a bola está no campo adversário quando está
**mais próxima de `golX` do que de `proprioGolX`**.

### 3.2. Mudança no ControladorDecisaoJogador

Quando `bola livre` e `papel == ZAGUEIRO` e `bolaNoFieldAdversario == true`:
- Substituir os pesos normais do zagueiro por pesos fortemente defensivos.
- Valor proposto: `PERSEGUIR_BOLA = 5`, `MANTER_POSICAO_DEFENSIVA = 95`.

Quando `bola livre` e `papel == ATACANTE` e `bolaNoFieldPropio == true`:
- Atacante mantém peso normal de perseguição, mas pode recuar levemente.
- Comportamento atual do atacante não muda nesta task — ele já tende a perseguir.

### 3.3. Novo campo em ContextoDecisao

`ContextoDecisao` passa a incluir:

```java
private final boolean bolaNoFieldAdversario;
```

Calculado a partir de `Ambiente.bola.x`, `golX` do jogador e `Ambiente.largura`.
O `ControladorDecisaoJogador` usa esse campo para ajustar os pesos.

### 3.4. Comportamento resultante

- Zagueiro com bola livre e bola no campo adversário → quase sempre mantém posição defensiva.
- Zagueiro com bola livre e bola no campo próprio → pesos normais (vai buscar 25%, mantém 75%).
- Atacante: comportamento atual preservado nesta task.

### 3.5. Arquivos alterados

| Arquivo | Mudança |
|---------|---------|
| `ContextoDecisao.java` | Adicionar `boolean bolaNoFieldAdversario` |
| `JogadorAgent.java` | Passar `bolaNoFieldAdversario` ao construir `ContextoDecisao` |
| `ControladorDecisaoJogador.java` | Usar `bolaNoFieldAdversario` nos pesos do zagueiro em `bola livre` |

---

## 4. Task 5 — Passe com Comunicação ACL

### 4.1. Princípios do design

- O atacante pode pedir o passe, mas nunca passa a bola quando ganha a posse.
- O zagueiro pode decidir passar sozinho, mas nunca envia pedidos de passe.
- A bola se move fisicamente no campo — sem teletransporte. O mecanismo é o mesmo do chute.
- O passador mira na **posição atual do receptor no momento do chute**.
  - O receptor continua se movendo durante a negociação — o passe pode errar (comportamento realista).
  - Futura melhoria (fora deste PRD): predição de trajetória para passes mais precisos.

### 4.2. Protocolo ACL

Novo `conversationId`: `"passe-bola"` (separado de `"disputa-bola"`).

**Fluxo 1 — Receptor (Atacante) inicia o pedido:**

```
Atacante  → REQUEST → Zagueiro   (conteúdo: "tipo=passe;solicitante=<nome>")
Zagueiro  → AGREE   → Atacante   (aceita; vai passar)
        ou REFUSE   → Atacante   (recusa; vai chutar)
[se AGREE]
Zagueiro chuta a bola em direção à posição ATUAL do Atacante
Atacante continua se movendo (posição no instante do chute é o alvo)
```

**Fluxo 2 — Passador (Zagueiro) inicia:**

```
Zagueiro  → INFORM  → Atacante   (conteúdo: "tipo=passe;passador=<nome>")
Atacante  → AGREE   → Zagueiro   (receptor confirma e se prepara)
Zagueiro chuta a bola em direção à posição ATUAL do Atacante
```

**Conflito entre os dois fluxos:**

Se o Zagueiro já decidiu passar (Fluxo 2) e recebe um REQUEST do Atacante
(Fluxo 1) ao mesmo tempo, o Zagueiro ignora o REQUEST (já está em `passePendente = true`).

### 4.3. Estado de passe nos agentes

Em `JogadorAgent`:

```java
private boolean passePendente = false;         // zagueiro: está executando um passe
private boolean aguardandoPasse = false;        // atacante: pediu ou recebeu INFORM de passe
private int ticksCooldownPasse = 0;            // cooldown após receber passe (evita loop)
```

Em `JogadorEstado` (serializado no WebSocket):

```java
public boolean aguardandoPasse;   // visível no frontend para debug
```

### 4.4. Força do passe

O passador normaliza o vetor até o receptor e aplica uma força fixa:

```text
distancia = calcularDistancia(passador.x, passador.y, receptor.x, receptor.y)
forcaX = (deltaX / distancia) * FORCA_PASSE
forcaY = (deltaY / distancia) * FORCA_PASSE
```

| Constante | Valor proposto | Justificativa |
|-----------|---------------|---------------|
| `FORCA_PASSE` | `1.0` | Mantém velocidade uniforme, independentemente da distância |
| `RAIO_PASSE` | `20.0` | Distância máxima para considerar passe viável |
| `TICKS_COOLDOWN_PASSE` | `10` | Evita loop imediato de passes entre os dois aliados |

### 4.5. Detecção de aliado em posição de passe

`SistemaFutebol` expõe:

```java
public Optional<String> localizarAliadoEmPosicaoDePasse(
    String nomeJogador, JogadorEstado estado, Time time)
```

Critérios para um aliado ser candidato ao passe:
- Pertence ao mesmo `time`.
- Está dentro de `RAIO_PASSE`.
- Não está em penalidade (`ticksPenalidadePerderDisputaRestantes == 0`).
- Não está em `ticksCooldownPasse > 0`.

### 4.6. Novo TipoDecisao

Adicionar `PASSAR_BOLA` ao enum `TipoDecisao`.

### 4.7. Decisão: chutar vs passar

Em `ControladorDecisaoJogador`, quando `eu_com_bola` e aliado em posição de passe disponível:

| Papel | Probab. de passar | Probab. de chutar |
|-------|-------------------|-------------------|
| Atacante | 0% | 100% |
| Zagueiro | 70% | 30% |

Quando nenhum aliado disponível: age com a bola (comportamento atual).

### 4.8. Arquivos alterados

| Arquivo | Mudança |
|---------|---------|
| `TipoDecisao.java` | Adicionar `PASSAR_BOLA` |
| `JogadorEstado.java` | Adicionar `boolean aguardandoPasse` |
| `SistemaFutebol.java` | Adicionar `localizarAliadoEmPosicaoDePasse(...)` |
| `ContextoDecisao.java` | Adicionar `Optional<String> aliadoEmPosicaoDePasse` |
| `JogadorAgent.java` | Adicionar `CyclicBehaviour` para `"passe-bola"`; `executarPasse()`; `receberPedidoPasse()`; `receberAvisoPasse()`; flags de estado |
| `ControladorDecisaoJogador.java` | Usar `aliadoEmPosicaoDePasse` na decisão de `eu_com_bola` |

---

## 5. Sequenciamento das tasks

| Task | Título | Depende de |
|------|--------|-----------|
| 3 | Chute dirigido ao gol | — |
| 4 | Posicionamento por zona (zagueiro tende ao campo defensivo) | Task 3 |
| 5 | Passe com comunicação ACL (dois iniciadores, mira na posição atual) | Task 4 |
| 6 | Validação de regressão PRD 3 | Task 5 |

---

## 6. Decisão de design documentada

**Por que mirar na posição atual e não prever trajetória:**

Mirar na posição atual do receptor no momento do chute é mais simples, produz
comportamento realista (o passe às vezes erra) e é suficiente para a primeira
implementação. A predição de trajetória (calcular onde o receptor vai estar ao
fim do deslocamento da bola) é uma melhoria futura deliberadamente deixada para
fora deste PRD — pode ser implementada como aprimoramento de perfil (receptores
mais "avançados" fazem passes com predição).

---

## 7. Invariantes a preservar

- `conversationId = "disputa-bola"` não é alterado.
- `BolaAgent` não é alterado em nenhuma das tasks deste PRD.
- A bola nunca teletransporta — o passe usa o mecanismo de chute existente.
- Nenhuma disputa entre aliados (regressão do PRD 2 preservada).
- Payload WebSocket: campos novos em `JogadorEstado` são aditivos.
