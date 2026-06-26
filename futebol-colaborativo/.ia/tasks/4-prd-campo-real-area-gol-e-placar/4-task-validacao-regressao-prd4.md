# Task 5: Validação e Regressão PRD 4

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** Tasks 2, 3 e 4

---

## Objetivo

Validar que as três tasks do PRD 4 funcionam corretamente, que os comportamentos do
PRD 3 foram preservados, e identificar qualquer ajuste de calibração necessário.

---

## Validação Automatizada

```powershell
cd futebol-colaborativo
mvn test
```

```powershell
cd frontend
npm run lint
npm run build
```

---

## Validação Manual

Terminal 1 — backend:

```powershell
cd futebol-colaborativo
mvn exec:java
```

Terminal 2 — frontend:

```powershell
cd frontend
npm run dev
```

Abrir `http://localhost:5173` no navegador.

---

## Checklist de verificação

### Task 2 — Campo 300×150

- [ ] Campo renderizado no frontend é visivelmente maior e ocupa bem a tela
- [ ] `Y_INICIAL=75`: todos os 4 jogadores iniciam na mesma linha horizontal (meio do campo)
- [ ] Time Azul: `atacante-1` aparece em x≈135, `zagueiro-1` em x≈60
- [ ] Time Vermelho: `atacante-2` aparece em x≈165, `zagueiro-2` em x≈240
- [ ] Jogadores se movem em velocidade razoável — campo não parece "lento"
- [ ] Bola chutada no início da partida chega ao outro lado do campo
- [ ] Após um gol, jogadores voltam corretamente às novas posições iniciais

### Task 3 — Área de gol real

- [ ] Quando a bola cruza a linha lateral dentro da área do gol (retângulo branco menor), o log exibe `X marcou um gol!`
- [ ] Quando a bola cruza a linha lateral fora da área do gol, a bola rebate normalmente e nenhum gol é registrado
- [ ] A área visual do gol no frontend coincide com a janela de detecção do backend
- [ ] Após o gol, a bola é reposicionada no centro e os jogadores voltam às posições iniciais

### Task 4 — Layout frontend

- [ ] Campo ("Campo da partida") ocupa toda a largura superior da tela
- [ ] "Estado da Jogada" e "Diagnóstico de payload" aparecem lado a lado abaixo do campo
- [ ] Sem scroll horizontal
- [ ] Em tela estreita (redimensionar janela para ≤ 1100px): painéis empilham verticalmente

### Regressão PRD 3 — comportamentos preservados

- [ ] Log mostra `chute dirigido | angulo_base=...° | desvio=...°` em vez de chute aleatório
- [ ] Bola vai aproximadamente para o lado do gol adversário na maioria dos chutes
- [ ] Quando bola está no campo adversário, zagueiro raramente vai buscá-la
- [ ] Quando bola está no campo próprio, zagueiro vai buscá-la normalmente
- [ ] Passes acontecem: log mostra `solicitando passe de ...` e `passe para ... | forca=(...)`
- [ ] A bola se move fisicamente de um jogador para o outro (sem teletransporte)
- [ ] Nenhuma disputa entre aliados (CFP só entre adversários nos logs)
- [ ] 4 agentes aparecem no campo com cores corretas (azul e vermelho)
- [ ] WebSocket conectado, painel "Diagnóstico" mostra status `connected`

### Calibração (itens qualitativos para ajuste se necessário)

- [ ] **Velocidade dos jogadores:** os jogadores não parecem lentos demais nem rápidos demais para o tamanho do campo
- [ ] **Força do chute:** a bola alcança o gol com frequência razoável sem atravessar o campo em menos de 2 segundos
- [ ] **Raio de contato:** jogadores conseguem pegar a bola normalmente (sem "correr pela bola sem pegar")
- [ ] **Passes:** a bola chega ao receptor com frequência razoável (passe não erra toda vez)

> Se algum item de calibração estiver fora do ponto ideal, registrar os ajustes necessários
> como nova task separada — não misturar calibração com as tasks deste PRD.

---

## Fora de Escopo

- Corrigir bugs encontrados nesta task (abrir nova task para cada bug).
- Alterar comportamento tático, probabilidades ou lógica de decisão.
- Adicionar features novas (goleiro, placar, zoom).

---

## Critérios de Sucesso

- `mvn test` passa sem falha.
- `npm run lint` e `npm run build` passam.
- Checklist de verificação executado sem bloqueadores críticos.
- Itens de calibração avaliados e documentados no Resultado.

---

## Resultado

- `mvn test` passou: 30 testes, 0 falhas.
- `npm run lint` passou sem erros.
- `npm run build` passou sem erros.
- Validação visual executada com backend e frontend rodando simultaneamente.
- Campo 300×150 renderizado corretamente; jogadores aparecem espaçados nas posições iniciais escaladas.
- Área de gol funcionando: bola detectada como gol apenas quando cruza a linha lateral dentro da área visual do gol; bola fora da área rebate normalmente.
- Layout frontend aplicado: campo ocupa toda a largura superior; painéis "Estado da Jogada" e "Diagnóstico" ficam lado a lado abaixo do campo.
- Comportamentos do PRD 3 preservados: chute dirigido, posicionamento por zona, passes com ACL e ausência de disputa entre aliados.
- Calibração sem bloqueadores: velocidade, força de chute, raio de contato e passes dentro do ponto ideal para o novo tamanho de campo.
- PRD 4 concluído.
