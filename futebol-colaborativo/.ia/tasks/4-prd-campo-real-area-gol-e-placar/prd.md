# PRD: Campo Real e Área de Gol

> **Escopo:** aumentar o campo de jogo, tornar toda a área do gol válida para marcar e exibir o placar da partida
> **Depende de:** PRD 3 (Colaboração Básica) — concluído

---

## 1. Resumo

Este PRD evolui a simulação em dois eixos independentes mas relacionados.

O primeiro é o tamanho do campo: o campo atual (100×60) é pequeno demais para o número de agentes e para a visibilidade das movimentações táticas. O campo passa para 300×150, mantendo a proporção 2:1 do futebol.

O segundo é a área de gol: hoje a detecção de gol usa um raio fixo de 2.4 unidades ao redor de um único ponto central (`golY = altura/2`). Isso significa que o jogador precisa chutar quase exatamente no centro para marcar. O objetivo é fazer com que qualquer chute que entre dentro da área do gol visual do frontend seja contado como gol — tornando a marcação mais natural e visualmente coerente.

O layout do frontend também é ajustado: o card do campo passa a ocupar toda a largura superior da tela, e os painéis laterais ("Estado da Jogada" e "Diagnóstico") vão para baixo, lado a lado.

O terceiro eixo é o placar: hoje os gols são registrados no log do terminal mas nenhum contador é mantido e nada é exibido ao usuário. O placar deve ser contado no backend e exibido no frontend dentro do card do campo, centralizado acima do canvas, no formato `AZUL  2 × 1  VERMELHO`.

---

## 2. Problema

### 2.1. Campo pequeno demais

Com `largura=100` e `altura=60`, os quatro jogadores ficam muito próximos uns dos outros.
Comportamentos táticos como posicionamento por zona, perseguição de bola e passe ficam visualmente comprimidos — é difícil observar o que está acontecendo.
O campo pequeno também amplifica o impacto de qualquer desvio de chute: qualquer erro angular manda a bola para longe do alvo imediatamente.

### 2.2. Gol como ponto único

A detecção atual valida gol apenas se `Math.abs(bola.y - golY) <= 2.4`.
Isso representa uma fatia de 4.8 unidades em um campo de altura 60 (~8% da altura).
No frontend, a área do gol desenhada visualmente tem altura de aproximadamente 20% do campo — mais de duas vezes maior do que a janela de detecção real.
O resultado é que a maioria dos chutes que visualmente "entrou no gol" não é registrada como gol, quebrando a coerência entre o que o usuário vê e o que o sistema detecta.

### 2.3. Layout do frontend não aproveita a tela

O campo atualmente divide a tela com os cards laterais. Com o campo maior, a área de visualização fica comprimida demais. Os painéis de estado e diagnóstico são informativos mas secundários — deveriam estar abaixo do campo, não ao lado.

---

## 3. Objetivo

- O campo de jogo tem dimensões 300×150 no backend e é renderizado proporcionalmente no frontend.
- Um gol é marcado quando a bola cruza a linha lateral (x≤0 ou x≥largura) dentro da faixa Y da área do gol, que corresponde à área desenhada no frontend (~20% da altura centrada).
- O layout do frontend coloca o campo em destaque na parte superior e os painéis de informação abaixo, lado a lado.

---

## 4. Usuário / contexto de uso

- Desenvolvedor observando comportamento tático e colaborativo no frontend.
- Professor/avaliador verificando a simulação durante a apresentação do projeto.

---

## 5. Escopo desta entrega

### 5.1. Dentro do escopo

**Novo tamanho de campo (backend):**
- `Ambiente.largura = 300`
- `Ambiente.altura = 150`
- `Ambiente.golY = 75` (centro vertical — sem mudança de lógica)
- Posições iniciais dos jogadores e resetPosição ajustadas para as novas dimensões.
- Limites de movimento atualizados para refletirem o novo campo.

**Área de gol real (backend):**
- Substituir `Math.abs(bola.y - golY) <= RAIO_GOL` por uma faixa: `bola.y >= GOL_Y_MIN && bola.y <= GOL_Y_MAX`.
- `GOL_Y_MIN` e `GOL_Y_MAX` são calculados com base em `FRACAO_ALTURA_GOL = 0.2016` (correspondente à proporção `0.36 × 0.56` usada pelo frontend).
- Com `altura=150`: `GOL_Y_MIN ≈ 59.88`, `GOL_Y_MAX ≈ 90.12` (±15.12 do centro).
- A constante `RAIO_GOL` do `Movimento.java` (usada para chegada perto do gol pelo jogador) não é afetada — esse é o raio de "estou perto do gol", não de "entrei no gol".

**Layout frontend:**
- O card do campo (`viewport-panel`) passa a ocupar toda a largura do `dashboard`.
- Os cards `Scoreboard` e `DebugPanel` vão para uma seção abaixo, lado a lado (dois em uma linha).
- O tamanho visual do campo (canvas Pixi) cresce para ocupar a largura disponível; o campo já é escalado proporcionalmente via `coordinateMapper`.

**Placar da partida:**
- Backend conta gols por time e inclui `placar: {A, B}` no payload WebSocket.
- Time AZUL é o "A"; time VERMELHO é o "B".
- Frontend lê o placar do payload e exibe centralizado acima do canvas: `AZUL  2 × 1  VERMELHO`, com os nomes dos times nas cores correspondentes (azul / vermelho).
- Placar reseta ao reiniciar o backend (em memória, sem persistência).

### 5.2. Fora do escopo

- Alterar a lógica tática dos jogadores (posicionamento, disputa, passe) além das posições iniciais.
- Adicionar representação visual de gol (trave, rede) ao frontend.
- Alterar a proporção ou o estilo visual do campo no frontend além do redimensionamento do layout.
- Mudar a velocidade de jogo, cooldowns ou qualquer parâmetro de gameplay.
- Goleiro ou lógica de impedimento.

---

## 6. Regra da área de gol

A fração da altura do campo que representa a área de gol é derivada da proporção que o frontend já usa para desenhar a `goalArea`:

```
penaltyHeight    = campoAltura × 0.36
goalAreaHeight   = penaltyHeight × 0.56
               = campoAltura × 0.36 × 0.56
               = campoAltura × 0.2016
```

A área do gol fica centrada em `golY = altura / 2`:

```
GOL_Y_MIN = golY - (altura × 0.2016 / 2)
GOL_Y_MAX = golY + (altura × 0.2016 / 2)
```

Com `altura=150`:
- `GOL_Y_MIN = 75 - 15.12 = 59.88`
- `GOL_Y_MAX = 75 + 15.12 = 90.12`

Gol ocorre quando:
- `bola.x <= 0` E `bola.y ∈ [GOL_Y_MIN, GOL_Y_MAX]` (gol no lado esquerdo), OU
- `bola.x >= largura` E `bola.y ∈ [GOL_Y_MIN, GOL_Y_MAX]` (gol no lado direito)

---

## 7. Posições iniciais ajustadas

Os jogadores precisam ter posições iniciais compatíveis com o novo tamanho do campo. As referências atuais assumem um campo 100×60. Com 300×150, as posições devem ser reescaladas proporcionalmente (fator 3x em X e 2.5x em Y), preservando a distribuição tática existente (atacante avançado, zagueiro recuado).

O mapeamento exato das posições é responsabilidade da TechSpec.

---

## 8. Critérios de aceite

- O campo no backend tem `largura=300` e `altura=150`; a simulação roda sem erros de compilação ou runtime.
- Após cada gol, o placar é incrementado corretamente: gol no lado esquerdo → VERMELHO +1; gol no lado direito → AZUL +1.
- O payload WebSocket inclui `placar: {A: <gols_azul>, B: <gols_vermelho>}`.
- O frontend exibe o placar acima do canvas, centralizado, no formato `AZUL  N × M  VERMELHO`, com cores distintas por time.
- Gols são registrados quando a bola cruza a linha lateral dentro da faixa Y calculada; chutes que "visualmente entram no gol" (de acordo com o frontend) são detectados pelo backend.
- O campo no frontend ocupa toda a largura superior; os painéis de estado e diagnóstico ficam abaixo, lado a lado.
- Nenhuma regressão nos comportamentos existentes: disputa, passe, chute dirigido e posicionamento por zona continuam funcionando.
- O campo renderiza proporcionalmente sem distorções; bola e jogadores aparecem em posições corretas.

---

## 9. Riscos

| Risco | Mitigação |
|-------|-----------|
| Posições iniciais inválidas no novo campo | Mapear explicitamente na TechSpec; validar visualmente no frontend |
| Jogadores indo para fora dos limites | Verificar `Movimento.java` com os novos limites de largura e altura |
| Área de gol maior pode gerar mais gols sem que isso pareça realista | Calibrar visualmente; `0.2016 × 150 ≈ 30` unidades de altura de gol é razoável |
| Frontend com campo grande quebrando proporção | O `coordinateMapper` já escala; apenas garantir que o CSS permita crescer |
| Constante `FRACAO_ALTURA_GOL` desacoplada do frontend | Documentar o valor `0.36 × 0.56` no código para rastreabilidade |
