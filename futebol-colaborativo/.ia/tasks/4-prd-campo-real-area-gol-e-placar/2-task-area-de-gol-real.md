# Task 3: Área de Gol Real

> **Status:** pending
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** Task 2 (campo 300×150 com `Ambiente.golYMin` e `Ambiente.golYMax`)

---

## Problema

A detecção de gol atual usa um raio fixo de 2.4 unidades ao redor do ponto central
`(x=0, y=golY)` e `(x=largura, y=golY)`:

```java
// SistemaFutebol.java, linha 322–323
boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura
    && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
```

Com campo 100×60, isso cobre apenas 4.8 unidades verticais (~8% da altura).
A área de gol desenhada no frontend tem altura ≈ 20% do campo — mais de 2× maior.
O resultado é que chutes que visualmente entram no gol não são detectados pelo backend.

Com o novo campo 300×150 (Task 2), a constante `RAIO_GOL = 2.4` representaria
apenas 1.6% da altura — ainda mais incongruente com o que o frontend desenha.

---

## Objetivo

Substituir a detecção por raio por uma detecção por faixa de Y, usando
`Ambiente.golYMin` e `Ambiente.golYMax` criados na Task 2. A faixa corresponde
exatamente à `goalArea` visual do frontend (proporção `0.36 × 0.56` da altura do campo).

Com campo 300×150:
- `golYMin ≈ 59.88` (75 − 150 × 0.2016 / 2)
- `golYMax ≈ 90.12` (75 + 150 × 0.2016 / 2)
- Janela de gol ≈ 30.24 unidades (~20% de 150)

---

## Escopo

Alterar apenas `SistemaFutebol.java`.

---

## Requisitos

### Remover a constante `RAIO_GOL`

Localizar na linha 25:

```java
private static final double RAIO_GOL = 2.4;
```

Remover essa linha inteiramente. Não existe mais em nenhum outro lugar de `SistemaFutebol`.

> Atenção: `Movimento.RAIO_GOL` (em `Movimento.java`) é uma constante diferente —
> usada pelo `JogadorAgent` para detectar "estou perto do gol para chutar".
> Essa constante **não é alterada** nesta task (já foi atualizada na Task 2 para 6.0).

### Atualizar `verificarGolDaBola()`

Localizar o método (linhas 321–331 no arquivo atual):

```java
private void verificarGolDaBola() {
    boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0
        && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;
    boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura
        && Math.abs(Ambiente.bola.y - Ambiente.golY) <= RAIO_GOL;

    if (!bolaNoGolEsquerdo && !bolaNoGolDireito) {
        return;
    }

    String marcador = localizarUltimoAtacante(bolaNoGolEsquerdo ? 0 : Ambiente.largura);
    registrarGol(marcador == null ? "Bola" : marcador);
}
```

Substituir por:

```java
private void verificarGolDaBola() {
    boolean emFaixaGol = Ambiente.bola.y >= Ambiente.golYMin
                      && Ambiente.bola.y <= Ambiente.golYMax;

    boolean bolaNoGolEsquerdo = Ambiente.bola.x <= 0   && emFaixaGol;
    boolean bolaNoGolDireito  = Ambiente.bola.x >= Ambiente.largura && emFaixaGol;

    if (!bolaNoGolEsquerdo && !bolaNoGolDireito) {
        return;
    }

    String marcador = localizarUltimoAtacante(bolaNoGolEsquerdo ? 0 : Ambiente.largura);
    registrarGol(marcador == null ? "Bola" : marcador);
}
```

### Por que a ordem `rebaterBolaNasBordas → verificarGolDaBola` é mantida

Em `aplicarFisicaBola()`:

```java
rebaterBolaNasBordas();   // (1) fixa x = 0 ou x = largura, inverte velocidade
verificarGolDaBola();      // (2) detecta gol com x == 0 ou x == largura
```

Quando a bola atravessa x=0 com y na faixa de gol:
1. `rebaterBolaNasBordas()` fixa `x = 0` e inverte `velocidadeX`
2. `verificarGolDaBola()` encontra `x <= 0` (true) e `y ∈ [golYMin, golYMax]` (true) → gol
3. `registrarGol()` reseta a bola para o centro — a inversão de velocidade do passo 1 é descartada

Quando a bola atravessa x=0 com y **fora** da faixa:
1. `rebaterBolaNasBordas()` fixa `x = 0` e inverte `velocidadeX`
2. `verificarGolDaBola()` encontra `y ∉ [golYMin, golYMax]` → sem gol
3. Bola continua com velocidade invertida (rebate na lateral)

Não é necessário trocar a ordem das chamadas.

---

## Fora de Escopo

- Alterar `rebaterBolaNasBordas()` — a bola ainda rebate na lateral fora da área de gol.
- Alterar `Ambiente.java` — `golYMin` e `golYMax` já existem após a Task 2.
- Alterar qualquer outro arquivo além de `SistemaFutebol.java`.
- Alterar `Movimento.RAIO_GOL` — esse raio é de chegada do jogador ao gol, não de detecção de gol.

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

- [ ] Backend compila e inicia sem erro
- [ ] Gols são registrados: quando a bola cruza a linha lateral dentro da área visual do gol, o log exibe `X marcou um gol!`
- [ ] Após um gol, a bola retorna ao centro e os jogadores voltam às posições iniciais
- [ ] Chutes que cruzam a linha lateral fora da área do gol **não** são registrados como gol — a bola rebate normalmente
- [ ] A área de gol no frontend (retângulo branco menor junto à linha lateral) corresponde visualmente aos gols detectados

### Log esperado ao marcar gol

```
atacante-1 marcou um gol!
Chute inicial aleatorio: forca (0.xx, 0.xx)
```

---

## Critérios de Sucesso

- `mvn test` passa.
- Gols são detectados quando a bola entra na faixa Y correta.
- Bola rebate na lateral fora da área de gol sem registrar gol.
- A constante `RAIO_GOL` não existe mais em `SistemaFutebol.java`.

---

## Resultado

_(preencher após execução)_
