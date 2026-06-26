# PRD: Comportamento Tático e Passe Colaborativo

> **Escopo:** evolução tática e colaborativa dos jogadores
> **Depende de:** PRD 2 (Dois Jogadores por Time) — concluído

---

## 1. Resumo

Este PRD evolui a simulação para um jogo visualmente mais parecido com futebol.
Três problemas são atacados juntos: o chute completamente aleatório, dois aliados
indo para a mesma bola ao mesmo tempo, e a ausência de qualquer passe entre companheiros.

A primeira entrega é o chute dirigido ao gol. A segunda é a divisão de papéis para
bola livre. A terceira é o passe com comunicação ACL — o aliado recebe um aviso,
se posiciona, e a bola se move fisicamente até ele (sem teletransporte).

---

## 2. Problema

### 2.1. Chute sem direção

Com `USAR_CHUTE_ALEATORIO_TESTE = true`, o jogador chuta com força completamente
aleatória. A bola vai para qualquer direção, inclusive de volta para o campo próprio.
Isso torna o jogo visualmente caótico e impede observar os papéis táticos funcionando.

### 2.2. Dois aliados indo para a mesma bola

Quando a bola está livre, atacante e zagueiro do mesmo time vão para ela ao mesmo tempo.
Não existe divisão de papéis: quem deve ir buscar e quem deve segurar posição? O resultado
visual é confuso — parece que os aliados competem entre si em vez de se complementar.

### 2.3. Sem colaboração entre aliados

Jogadores do mesmo time nunca cooperam. Quando o atacante está com a bola e o zagueiro
está em posição adiantada, a única ação disponível é conduzir sozinho. Não existe passe.

---

## 3. Objetivo

Fazer a simulação parecer mais com futebol visualmente:

- A bola vai *aproximadamente* para o lado do gol adversário na maioria dos chutes.
- Aliados dividem a responsabilidade pela bola livre (atacante vai buscar, zagueiro segura posição).
- Jogadores passam a bola entre si via comunicação ACL, com a bola se movendo fisicamente no campo.

---

## 4. Usuário / contexto de uso

- Desenvolvedor observando comportamento tático no frontend.
- Professor/avaliador verificando colaboração entre agentes na simulação.

---

## 5. Escopo desta entrega

### 5.1. Dentro do escopo

**Chute dirigido:**
- Jogador com bola chuta na direção geral do gol adversário.
- Desvio angular aleatório: a maioria dos chutes vai para o lado certo, mas ocasionalmente
  erra feio (inclusive para o campo próprio) para parecer comportamento humano.

**Divisão de papéis na bola livre:**
- Quando a bola está livre, apenas o atacante vai buscá-la ativamente.
- O zagueiro mantém posição defensiva quando a bola está livre e o atacante aliado está em campo.
- Quando o atacante aliado não estiver disponível (penalidade, fora de posição), o zagueiro vai buscar.

**Passe com comunicação ACL:**
- Jogador com bola detecta aliado próximo e disponível para receber.
- Passador envia mensagem ACL avisando que vai passar (`tipo=passe`).
- Receptor recebe o aviso, registra que vai receber e se move em direção ao ponto de chegada estimado.
- Passador chuta a bola fisicamente em direção ao receptor — a bola se move no campo.
- Receptor coleta a bola normalmente quando ela chega perto.

### 5.2. Fora do escopo

- Marcação coletiva / cobertura de zona.
- Detecção de adversário marcando o aliado.
- Lógica de quando NÃO passar (hoje: se aliado disponível, pode passar).
- Goleiro.
- Probabilidades dinâmicas durante a partida.
- Melhorias de interceptação ou posicionamento além dos itens acima.

---

## 6. Comportamento esperado por papel após PRD 3

### Atacante

- Quando bola livre: vai buscar ativamente.
- Quando aliado zagueiro tem a bola: se posiciona à frente para receber passe.
- Quando tem a bola: chuta dirigido ao gol ou passa para aliado próximo.

### Zagueiro

- Quando bola livre e atacante aliado em campo: mantém posição defensiva.
- Quando bola livre e atacante aliado indisponível: vai buscar.
- Quando tem a bola: pode passar para o atacante aliado próximo; caso contrário, chuta.

---

## 7. Regra de passe

Passador considera passar quando:
- Aliado do mesmo time está dentro do raio de passe.
- Aliado está dentro de um raio de passe razoável.
- Aliado não está em penalidade.

A decisão de passar vs chutar é influenciada pelo perfil tático:
- Atacante: prefere chutar quando próximo do gol; prefere passar quando longe.
- Zagueiro: prefere passar para o atacante quando ele está próximo.

---

## 8. Critérios de aceite

- Com `USAR_CHUTE_ALEATORIO_TESTE = true`, a bola vai para o lado do gol adversário
  na maioria dos chutes. Desvios grandes são aceitáveis e ocasionais.
- Quando a bola está livre, o atacante vai buscá-la e o zagueiro mantém posição (visível no frontend).
- Passes acontecem: a bola se move no campo de um jogador para outro, sem teletransporte.
- Nenhuma disputa entre aliados ocorre (regressão do PRD 2 preservada).
- O campo continua sendo disputado e gols continuam sendo registrados.

---

## 9. Riscos

| Risco | Mitigação |
|-------|-----------|
| Desvio muito alto → chute parece aleatório de novo | Calibrar visualmente; 120° max como ponto de partida |
| Zagueiro nunca vai buscar a bola (atacante em penalidade) | Garantir fallback: zagueiro vai buscar quando atacante indisponível |
| ACL de passe conflita com ACL de disputa | Usar `conversationId` separado: `"passe-bola"` |
| Aliados trocando passes em loop sem progredir | Limitar a um passe por posse; exigir avanço de posição |
| Bola não chega ao receptor (força errada) | Calibrar força do passe pela distância ao aliado |
