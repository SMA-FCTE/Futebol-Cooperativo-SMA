# Task 4: Frontend - Cor dos Jogadores por Time

> **Status:** completed
> **PRD:** `prd.md`
> **TechSpec:** `techspec.md`

## Objetivo

Fazer os circulos dos jogadores aparecerem na cor do time (azul ou vermelho) em vez de
laranja/amarelo. A cor ja esta implementada em `PlayerLayer.getTeamColor`, mas nao e ativada
porque o normalizador atual sempre resolve `team` como `'unknown'`.

## Causa Raiz

Em `normalizers.ts`, a funcao `normalizeLegacyPlayer` define:

```typescript
team: 'unknown',
```

Isso faz todos os jogadores aparecerem na cor padrao (laranja). O `PlayerLayer` ja sabe
colorir por time quando `team` e `'A'` ou `'B'`.

## Escopo

Alterar apenas `frontend/src/game/model/normalizers.ts`.

## Requisitos

Adicionar a funcao utilitaria:

```typescript
function inferTeamFromId(id: string): TeamId {
  if (id.startsWith('azul')) return 'A'
  if (id.startsWith('vermelho')) return 'B'
  return 'unknown'
}
```

Substituir em `normalizeLegacyPlayer`:

```typescript
// antes:
team: 'unknown',

// depois:
team: inferTeamFromId(id),
```

Nenhuma outra mudanca e necessaria. O `PlayerLayer` ja exibe o nome do jogador como label
(`node.label.text = player.id`), entao os nomes `azul-atacante`, `azul-zagueiro`,
`vermelho-atacante`, `vermelho-zagueiro` aparecerao automaticamente.

## Fora de Escopo

- Alterar `PlayerLayer.ts`.
- Alterar `gameTypes.ts`.
- Alterar qualquer arquivo do backend.
- Adicionar icones ou formas diferentes por papel.

## Validacao

```powershell
cd frontend
npm run build
npm run dev
```

Verificar no campo:
- 2 circulos azuis (`azul-atacante`, `azul-zagueiro`).
- 2 circulos vermelhos (`vermelho-atacante`, `vermelho-zagueiro`).
- Nomes visiveis acima dos circulos ao aproximar o zoom.

## Criterios de Sucesso

- Build do frontend passa sem erros.
- Circulos azuis para time azul, vermelhos para time vermelho.
- Nomes dos agentes visiveis no campo.
