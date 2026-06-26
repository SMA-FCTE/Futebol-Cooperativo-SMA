# Task 6: Validação de Regressão PRD 3

> **Status:** pending
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`
> **Depende de:** Tasks 3, 4 e 5

---

## Objetivo

Validar que o PRD 3 funciona corretamente e que nenhum comportamento anterior foi quebrado.

---

## Validação Automatizada

```powershell
cd futebol-colaborativo
mvn test
```

---

## Validação Manual

```powershell
cd futebol-colaborativo
mvn exec:java
```

Em outro terminal:

```powershell
cd frontend
npm run dev
```

---

## Checklist de verificação

### Chute dirigido (Task 3)

- [ ] Log mostra `chute dirigido | angulo_base=...° | desvio=...°`
- [ ] A bola vai para o lado do gol adversário na maioria dos chutes
- [ ] Ocasionalmente a bola vai para o lado errado (desvio > 90° possível)
- [ ] Nenhum log de `chute aleatorio de teste`

### Posicionamento por zona (Task 4)

- [ ] Quando bola está no campo adversário, zagueiro raramente vai buscá-la
- [ ] Quando bola está no campo próprio, zagueiro vai buscá-la normalmente
- [ ] Atacante continua perseguindo a bola independente do lado do campo
- [ ] Zagueiro e atacante do mesmo time não vão para a mesma bola ao mesmo tempo com frequência

### Passe com ACL (Task 5)

- [ ] Log mostra `solicitando passe de ...` quando atacante pede a bola
- [ ] Log mostra `passe para ... | forca=(...)` quando o passe é executado
- [ ] A bola se move fisicamente de um jogador para o outro (sem teletransporte)
- [ ] Atacante continua se movendo durante a negociação do passe
- [ ] Ocasionalmente o passe erra porque o receptor se moveu (esperado)
- [ ] Nenhum loop de passes (dois aliados passando de volta e para frente indefinidamente)

### Regressão (PRD 2 preservado)

- [ ] Nenhum CFP de disputa entre aliados nos logs
- [ ] Disputas entre adversários continuam ocorrendo normalmente
- [ ] 4 agentes aparecem no campo com cores corretas
- [ ] Gols continuam sendo registrados e jogadores voltam às posições iniciais
- [ ] WebSocket continua recebendo estado sem erro crítico

---

## Fora de Escopo

- Corrigir bugs encontrados nesta task (abrir nova task para cada bug).
- Avaliar se o comportamento tático é "bom" — isso é observação qualitativa para o futuro.

---

## Critérios de Sucesso

- Checklist acima executado sem bloqueadores críticos.
- Riscos restantes documentados na seção de resultado abaixo.

---

## Resultado

_(preencher após execução)_
