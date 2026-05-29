# Task 8: Validar Regressao da Refatoracao OODA

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`

## Objetivo

Validar que a refatoracao OODA preservou a simulacao atual e nao quebrou backend, WebSocket ou frontend.

## Escopo

- Rodar testes automatizados do backend.
- Subir backend.
- Validar criacao de agentes.
- Validar movimento dos jogadores.
- Validar disputa de bola.
- Validar que o frontend continua recebendo estado.

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

Verificar:

- agentes `azul`, `vermelho` e `bola` existem;
- jogadores se movem;
- bola atualiza posicao;
- disputa ainda aparece quando ocorre;
- frontend renderiza campo sem erro critico.

## Fora de Escopo

- Corrigir bugs encontrados.
- Criar novas features.
- Criar PR.

## Criterios de Sucesso

- Validacoes executadas ou bloqueios documentados.
- Riscos restantes anotados para a proxima etapa.

## Resultado

- Testes automatizados: `BUILD SUCCESS` com 10 testes (executado na Task 7).
- Validacao manual executada em 2026-05-29:
  - Backend subiu com `mvn exec:java`.
  - Frontend subiu com `npm run dev`.
  - Agentes `azul`, `vermelho` e `bola` criados corretamente.
  - Jogadores em movimento e bola atualizando posicao.
  - Frontend renderizando campo sem erro critico.
- PRD 1 (Refatoracao OODA) concluido.
