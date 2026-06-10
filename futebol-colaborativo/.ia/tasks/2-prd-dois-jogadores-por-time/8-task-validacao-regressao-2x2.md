# Task 5: Validacao de Regressao 2x2

> **Status:** pending
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Validar que a simulacao 2x2 funciona corretamente e que nenhum comportamento
anterior foi quebrado.

## Validacao Automatizada

```powershell
cd futebol-colaborativo
mvn test
```

## Validacao Manual

```powershell
cd futebol-colaborativo
mvn exec:java
```

Em outro terminal:

```powershell
cd frontend
npm run dev
```

### Checklist de verificacao

**Agentes e posicionamento:**
- [ ] 4 agentes criados nos logs: `azul-atacante`, `azul-zagueiro`, `vermelho-atacante`, `vermelho-zagueiro`
- [ ] 4 circulos aparecem no campo
- [ ] Circulos azuis no lado esquerdo, vermelhos no lado direito ao iniciar
- [ ] Nomes visiveis acima dos circulos

**Times e disputa:**
- [ ] Nenhum CFP de disputa entre `azul-*` e `azul-*` nos logs
- [ ] Nenhum CFP de disputa entre `vermelho-*` e `vermelho-*` nos logs
- [ ] Disputas ocorrem normalmente entre `azul-*` e `vermelho-*`

**Comportamento geral:**
- [ ] Jogadores se movem no campo
- [ ] Bola e disputada e conduzida
- [ ] Gol e registrado quando a bola entra no gol
- [ ] Jogadores voltam as posicoes iniciais apos gol
- [ ] WebSocket/frontend continua recebendo estado sem erro critico

## Fora de Escopo

- Corrigir bugs encontrados nesta task (abrir nova task para cada bug).
- Avaliar se o comportamento tatico e "bom" — isso e observacao qualitativa para o futuro.

## Criterios de Sucesso

- Checklist acima executado sem bloqueadores criticos.
- Riscos restantes documentados na secao de resultado abaixo.

## Resultado

_(preencher apos execucao)_
