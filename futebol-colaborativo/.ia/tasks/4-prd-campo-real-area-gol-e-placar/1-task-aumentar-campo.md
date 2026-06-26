# Task 2: Aumentar Dimensões do Campo

> **Status:** pending
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** —

---

## Problema

O campo atual mede 100×60 unidades. Com 4 jogadores em campo, as distâncias são pequenas demais
para observar comportamentos táticos — posicionamento por zona, perseguição de bola e passe
ficam comprimidos visualmente. Além disso, todas as constantes de distância, raio e força
foram calibradas para esse campo e perdem sentido com as novas dimensões.

---

## Objetivo

Alterar o campo para 300×150 unidades, reescalando (×3 em largura, ×2.5 em altura)
todas as constantes que dependem das dimensões do campo, para preservar a mesma dinâmica
relativa de jogo. O campo deve ser renderizado proporcionalmente no frontend sem nenhuma
alteração no `coordinateMapper` — ele já é 100% escalável.

---

## Escopo

Alterar 6 arquivos:
- `model/Ambiente.java`
- `App.java`
- `model/JogadorEstado.java`
- `agentes/JogadorAgent.java`
- `movimento/Movimento.java`
- `frontend/src/game/model/gameState.ts`

---

## Requisitos

### 1. `model/Ambiente.java`

Arquivo atual (completo):

```java
public class Ambiente {
    public static int largura = 100;
    public static int altura = 60;

    public static Bola bola = new Bola();

    public static int golX = largura;
    public static int golY = altura / 2;
}
```

Substituir por:

```java
public class Ambiente {
    public static int largura = 300;
    public static int altura  = 150;

    public static Bola bola = new Bola();

    public static int golX = largura;
    public static int golY = altura / 2;   // 75

    // Fração da altura que o frontend usa para desenhar a goalArea: penaltyHeight*0.56 = 0.36*0.56
    private static final double FRACAO_ALTURA_GOL = 0.36 * 0.56;
    public static double golYMin = golY - (altura * FRACAO_ALTURA_GOL / 2);  // ≈ 59.88
    public static double golYMax = golY + (altura * FRACAO_ALTURA_GOL / 2);  // ≈ 90.12
}
```

> `golYMin` e `golYMax` são usados pela Task 3 (área de gol). Criá-los aqui centraliza
> a lógica e evita duplicação em `SistemaFutebol`.

### 2. `App.java`

Localizar o bloco de constantes de posição (linhas 17–21 no arquivo atual):

```java
private static final double Y_INICIAL            = 30;
private static final double AZUL_ATACANTE_X      = 45;
private static final double AZUL_ZAGUEIRO_X      = 20;
private static final double VERMELHO_ATACANTE_X  = 55;
private static final double VERMELHO_ZAGUEIRO_X  = 80;
```

Substituir pelos valores reescalados (X×3, Y×2.5):

```java
private static final double Y_INICIAL            = 75;
private static final double AZUL_ATACANTE_X      = 135;
private static final double AZUL_ZAGUEIRO_X      = 60;
private static final double VERMELHO_ATACANTE_X  = 165;
private static final double VERMELHO_ZAGUEIRO_X  = 240;
```

Nenhum outro trecho de `App.java` precisa ser alterado. `Ambiente.largura` é referenciado
diretamente na criação dos jogadores (como `golX`), então já receberá o valor 300.

### 3. `model/JogadorEstado.java`

Linha 8 atual:

```java
public double velocidade = 1.0;
```

Alterar para:

```java
public double velocidade = 3.0;
```

> Campo 3× maior. Com velocidade 1.0 o jogador levaria 300 ticks para cruzar o campo
> (vs. 100 antes). Escalar para 3.0 mantém o mesmo tempo relativo de travessia.

### 4. `agentes/JogadorAgent.java`

Localizar o bloco de constantes no topo da classe (linhas 30–50 aproximadamente).
Alterar **somente** as seis constantes abaixo — nenhuma outra linha do arquivo muda:

```java
// antes → depois
private static final double DISTANCIA_CHUTE_AO_GOL             = 10.0;  →  30.0
private static final double FORCA_CHUTE                         = 1.65;  →  5.0
private static final double FORCA_CHUTE_DIRIGIDO                = 1.35;  →  4.0
private static final double DISTANCIA_POSICAO_DEFENSIVA_DO_GOL  = 8.0;   →  24.0
private static final double DISTANCIA_POSICAO_OFENSIVA_DO_GOL   = 35.0;  →  105.0
private static final double FORCA_PASSE                         = 1.0;   →  3.0
```

Bloco completo após a alteração (para referência de contexto — não alterar as outras linhas):

```java
static final String CONVERSA_ID_DISPUTA = "disputa-bola";
static final String CONVERSA_ID_PASSE   = "passe-bola";
private static final boolean USAR_CHUTE_ALEATORIO_TESTE = true;
private static final int    TICKS_PENALIDADE_PERDER_DISPUTA     = 20;
private static final double DISTANCIA_CHUTE_AO_GOL              = 30.0;
private static final double FORCA_CHUTE                         = 5.0;
private static final double MAX_DESVIO_ANGULO = Math.PI / 6.0;
private static final double FORCA_CHUTE_DIRIGIDO                = 4.0;
private static final double DISTANCIA_POSICAO_DEFENSIVA_DO_GOL  = 24.0;
private static final double DISTANCIA_POSICAO_OFENSIVA_DO_GOL   = 105.0;
private static final double FORCA_PASSE                         = 3.0;
private static final int    TICKS_COOLDOWN_PASSE                = 10;
private static final int    TICKS_TIMEOUT_NEGOCIACAO_PASSE      = 10;
private static final int    TICKS_TIMEOUT_RECEPCAO_PASSE        = 45;
private static final int    TICKS_JANELA_SOLICITACAO_PASSE      = 2;
private static final int    TICKS_BACKOFF_SOLICITACAO_PASSE     = 5;
```

### 5. `movimento/Movimento.java`

Localizar as três constantes públicas no topo da classe (linhas 8–12):

```java
public static final double RAIO_CONTATO_BOLA     = 1.2;
public static final double RAIO_PERSEGUICAO_BOLA = 10.0;
public static final double RAIO_GOL              = 2.0;
```

Substituir pelos valores ×3:

```java
public static final double RAIO_CONTATO_BOLA     = 3.6;
public static final double RAIO_PERSEGUICAO_BOLA = 30.0;
public static final double RAIO_GOL              = 6.0;
```

Nenhum outro trecho de `Movimento.java` muda. `limitarAoCampo` já usa `Ambiente.largura`
e `Ambiente.altura` diretamente — receberá os novos valores automaticamente.

### 6. `frontend/src/game/model/gameState.ts`

Localizar as linhas 3–6:

```ts
export const DEFAULT_FIELD_DIMENSIONS = {
  width: 100,
  height: 60,
} as const
```

Substituir por:

```ts
export const DEFAULT_FIELD_DIMENSIONS = {
  width: 300,
  height: 150,
} as const
```

Nenhum outro arquivo do frontend precisa ser alterado para esta task.
O `coordinateMapper` (`createFieldViewport`) escala coordenadas de campo para pixels
usando `field.width` e `field.height` — o ajuste é automático.

---

## Fora de Escopo

- Alterar a detecção de gol em `SistemaFutebol` (Task 3).
- Alterar layout do frontend (Task 4).
- Alterar física da bola (`ATRITO_BOLA`, `RESTITUICAO_BORDA`, `VELOCIDADE_MINIMA_BOLA`).
- Alterar `RAIO_PASSE` em `SistemaFutebol` (100.0 — mantido; calibração na task de validação).
- Alterar qualquer lógica de decisão dos jogadores.

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
npm install
npm run dev
```

### Checklist visual

- [ ] Backend inicia sem erro de compilação nem exceção em runtime
- [ ] Frontend conecta via WebSocket e exibe o campo
- [ ] O campo é visivelmente maior — jogadores aparecem em posições espalhadas no campo
- [ ] `Y_INICIAL=75` — todos os jogadores iniciam na linha horizontal do meio do campo
- [ ] Time Azul: atacante-1 em x≈135, zagueiro-1 em x≈60
- [ ] Time Vermelho: atacante-2 em x≈165, zagueiro-2 em x≈240
- [ ] Jogadores se movem normalmente — velocidade de traversia parece semelhante ao campo antigo
- [ ] Bola chutada no início chega ao outro lado do campo (não para no meio)
- [ ] `npm run lint` passa sem erros
- [ ] `npm run build` passa sem erros

---

## Critérios de Sucesso

- `mvn test` passa.
- `mvn exec:java` sobe sem erro.
- Campo exibido no frontend com dimensões 300×150 (jogadores visivelmente mais espaçados).
- Bola alcança o gol após ser chutada (forças escaladas corretamente).

---

## Resultado

_(preencher após execução)_
