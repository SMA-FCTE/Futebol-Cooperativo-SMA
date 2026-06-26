# PRD: Dois Jogadores por Time (2x2)

> **Escopo:** expansao da simulacao de 1x1 para 2x2 com papeis taticos basicos
> **Depende de:** PRD 1 (Refatoracao OODA) — concluido

---

## 1. Resumo

Expandir a simulacao de 1 jogador por time para 2 jogadores por time, introducindo papeis taticos
(atacante e zagueiro) com comportamentos distintos baseados em probabilidades. Cada time tera
1 atacante e 1 zagueiro. A diferenca de comportamento entre os papeis e expressa pelos pesos do
`PerfilTatico`, ja existente na arquitetura OODA.

---

## 2. Problema

Hoje a simulacao roda com 1 jogador azul contra 1 jogador vermelho. Nao existe diferenciacao
entre aliados e adversarios, nem papeis taticos. O sistema precisa:

- evitar disputa de bola entre jogadores do mesmo time;
- diferenciar comportamento de atacante e zagueiro por tendencias probabilisticas;
- posicionar jogadores de acordo com seu papel inicial;
- exibir no frontend os 4 jogadores com identificacao visual de time.

---

## 3. Objetivo

Ter uma simulacao funcional com 4 agentes: `azul-atacante`, `azul-zagueiro`,
`vermelho-atacante` e `vermelho-zagueiro`, onde cada um age de forma individual
porem com tendencias taticas distintas baseadas em seu papel.

---

## 4. Usuarios / contexto de uso

- Desenvolvedor observando comportamento multiagente no frontend.
- Professor/avaliador verificando a simulacao em execucao.

---

## 5. Escopo desta entrega

### 5.1. Dentro do escopo

- Criar 4 agentes com nomes `azul-atacante`, `azul-zagueiro`, `vermelho-atacante`, `vermelho-zagueiro`.
- Cada jogador comeca posicionado no seu lado do campo, com atacante mais proximo do centro e zagueiro mais proximo do proprio gol.
- `PerfilTatico.atacante()` com peso maior para perseguir a bola.
- `PerfilTatico.zagueiro()` com peso maior para manter posicao defensiva.
- Regra de disputa filtrada por time: so jogadores de times diferentes disputam a bola.
- Frontend com circulos coloridos por time (azul para `Time.AZUL`, vermelho para `Time.VERMELHO`) e nome do jogador visivel acima do icone.

### 5.2. Fora do escopo

- Passe entre aliados.
- Comunicacao tatica ACL entre companheiros de time.
- Deteccao de marcacao adversaria.
- Logica coletiva (quem vai a bola, quem cobre).
- Goleiro.
- Probabilidades dinamicas que mudam durante a partida.

---

## 6. Comportamento esperado por papel

### Atacante

- Tende a perseguir a bola com mais frequencia quando ela esta livre.
- Quando sem bola e adversario conduz, tenta interceptar.
- Quando com bola, conduz em direcao ao gol adversario.
- Posicao inicial: proximo a linha do meio-campo, no proprio lado do campo.

### Zagueiro

- Tende a manter posicao defensiva com mais frequencia quando a bola esta livre.
- Quando sem bola e adversario conduz, tenta interceptar.
- Quando com bola, conduz em direcao ao gol adversario.
- Posicao inicial: proximo ao proprio gol.

A diferenca e probabilistica, nao absoluta: os dois papeis podem tanto atacar quanto defender.

---

## 7. Regra de disputa entre times

Apenas jogadores de times diferentes podem iniciar disputa de bola.
Dois jogadores do mesmo time proximos um do outro ignoram a presenca do aliado
e seguem suas decisoes individuais normais.

---

## 8. Visual no frontend

- Circulo colorido: azul (`#1d4ed8`) para time azul, vermelho (`#dc2626`) para time vermelho.
- Nome do jogador exibido acima do icone (ja existe no frontend, sera preenchido com o nome completo).
- A cor do time sera inferida pelo frontend a partir do prefixo do nome do agente:
  - `azul-*` → time A (azul)
  - `vermelho-*` → time B (vermelho)
- Nenhuma mudanca no payload do WebSocket e necessaria para isso.

---

## 9. Criterios de aceite

- 4 agentes criados e visiveis no campo ao iniciar o backend.
- Agentes do time azul aparecem como circulos azuis; vermelho como circulos vermelhos.
- Nomes `azul-atacante`, `azul-zagueiro`, `vermelho-atacante`, `vermelho-zagueiro` visiveis no frontend.
- Dois jogadores do mesmo time proximos nao iniciam disputa entre si.
- Jogadores de times opostos continuam disputando a bola normalmente.
- O campo continua sendo disputado e gols continuam sendo registrados.

---

## 10. Riscos

| Risco | Mitigacao |
|---|---|
| Quatro agentes aumentam carga de disputas ACL simultaneas | Monitorar no terminal; ajustar tick se necessario |
| Comportamento dos aliados pode parecer confuso (dois indo para a mesma bola) | Esperado nesta entrega; logica coletiva e escopo do PRD 3 |
| Cor inferida pelo prefixo do nome pode quebrar se o nome mudar | Nome e convencao desta entrega; documentar na TechSpec |

---

## 11. O que nao implementamos e por que

| Item | Motivo |
|---|---|
| Passe entre aliados | Requer comunicacao ACL tatica e deteccao de companheiro livre; escopo do PRD 3 |
| Deteccao de marcacao | Requer rastreamento de adversario proximo ao aliado; escopo do PRD 3 |
| Comunicacao tatica | Requer novos tipos de mensagem ACL; escopo do PRD 3 |
| Decisao coletiva (quem vai a bola) | Requer coordenacao entre agentes; escopo do PRD 3 |
