# Task 7: Separar Nome, Time e Papel no Payload do WebSocket

> **Status:** completed
> **PRD:** `1-prd.md`
> **TechSpec:** `2-techspec.md`
> **Depende de:** Tasks 1–6 (perfis, bootstrap, filtro, frontend cor, threshold)
> **Deve ser executada antes de:** Task 8 (validacao de regressao)

## Problema

`JogadorEstado` — o DTO serializado pelo Gson e enviado via WebSocket — nao carrega `time`
nem `papel`. O frontend infere o time pelo prefixo do nome do jogador (`azul-*` → azul,
`vermelho-*` → vermelho). Isso acopla o nome do jogador a sua afiliacao de time: renomear
um agente para `"Breno"` ou um time para `"Palmeiras"` quebra a renderizacao.

No ciclo OODA, `JogadorEstado` representa o estado observavel do jogador no ambiente.
Time e papel sao propriedades permanentes do jogador — fazem parte do que o ambiente deve
expor para quem observa (inclusive o frontend).

## Objetivo

Fazer com que `JogadorEstado` carregue `time` e `papel` como campos explicitos, de modo que:

- o nome do jogador seja apenas um nome, sem convencao de prefixo;
- o frontend leia o time diretamente do payload, nao do nome;
- seja possivel chamar um jogador de "Breno" ou um time de "Palmeiras" sem quebrar nada.

## Escopo

Alterar `JogadorEstado.java`, `SistemaFutebol.java`, `App.java`, `gameTypes.ts`,
`PlayerLayer.ts` e `normalizers.ts`.

---

## Requisitos

### JogadorEstado.java

Adicionar dois campos publicos ao lado dos existentes:

```java
public String time;   // "AZUL" ou "VERMELHO"
public String papel;  // "ATACANTE" ou "ZAGUEIRO"
```

Estes campos sao imutaveis durante a partida — setados uma vez em `criarEstadoInicial`
e nunca sobrescritos. O Gson os serializa automaticamente no JSON enviado via WebSocket,
sem nenhuma outra mudanca no pipeline de envio.

### SistemaFutebol.java — criarEstadoInicial

Adicionar a inicializacao dos novos campos logo apos `estado.golX`:

```java
estado.time  = configuracao.getTime().name();   // "AZUL" ou "VERMELHO"
estado.papel = configuracao.getPapel().name();   // "ATACANTE" ou "ZAGUEIRO"
```

Nenhuma outra mudanca no metodo nem em outros metodos da classe.
O filtro de aliados em `localizarOponenteProximoParaDisputa` continua usando
`confAtual.getTime()` (enum Java direto do `ConfiguracaoJogador`) — nao depende desse campo.

### App.java

Renomear os 4 agentes para nomes que nao codificam time nem papel.
O time e o papel ja sao passados como parametros separados em `ConfiguracaoJogador`.

Exemplo de nomes validos (podem ser substituidos por qualquer string valida para o JADE):

| Nome atual       | Nome novo (sugestao) | Time            | Papel                 |
|---|---|---|---|
| `azul-atacante`  | `atacante-1`         | `Time.AZUL`     | `PapelJogador.ATACANTE` |
| `azul-zagueiro`  | `zagueiro-1`         | `Time.AZUL`     | `PapelJogador.ZAGUEIRO` |
| `vermelho-atacante` | `atacante-2`      | `Time.VERMELHO` | `PapelJogador.ATACANTE` |
| `vermelho-zagueiro` | `zagueiro-2`      | `Time.VERMELHO` | `PapelJogador.ZAGUEIRO` |

Nomes de agente no JADE nao podem conter espacos. Qualquer outro formato (ex: `"Breno"`,
`"defensor-a"`) tambem e valido. As constantes de posicao (`AZUL_ATACANTE_X`, etc.)
podem ser renomeadas de forma coerente ou mantidas — nao ha obrigacao.

### gameTypes.ts

**1.** Alterar `TeamId` para usar os identificadores do backend:

```typescript
// antes:
export type TeamId = 'A' | 'B' | 'unknown'

// depois:
export type TeamId = 'AZUL' | 'VERMELHO' | 'unknown'
```

**2.** Adicionar `time?: unknown` a `LegacyPlayerPayload` (campo que o backend passa a enviar):

```typescript
export type LegacyPlayerPayload = {
  x?: unknown
  y?: unknown
  velocidade?: unknown
  theta?: unknown
  comBola?: unknown
  golX?: unknown
  time?: unknown    // novo: "AZUL" ou "VERMELHO"
  papel?: unknown   // novo: "ATACANTE" ou "ZAGUEIRO" (serializado pelo Gson, ignorado pelo frontend por ora)
}
```

### PlayerLayer.ts

Atualizar `getTeamColor` para mapear os novos valores de `TeamId`:

```typescript
private getTeamColor(team: TeamId): number {
  switch (team) {
    case 'AZUL':
      return 0x1d4ed8
    case 'VERMELHO':
      return 0xdc2626
    default:
      return 0xf59e0b
  }
}
```

### normalizers.ts

**1.** Em `normalizeLegacyPlayer`, substituir:

```typescript
// antes:
team: inferTeamFromId(id),

// depois:
team: normalizeTeam(value.time),
```

**2.** Atualizar `normalizeTeam` para aceitar `'AZUL'` e `'VERMELHO'`:

```typescript
function normalizeTeam(value: unknown): TeamId {
  return value === 'AZUL' || value === 'VERMELHO' ? value : 'unknown'
}
```

**3.** Remover a funcao `inferTeamFromId` inteiramente.

---

## Arquivos alterados

| Arquivo | Mudanca |
|---|---|
| `JogadorEstado.java` | Adicionar `String time` e `String papel` |
| `SistemaFutebol.java` | Inicializar `time` e `papel` em `criarEstadoInicial` |
| `App.java` | Renomear agentes — nome desacoplado de time e papel |
| `gameTypes.ts` | `TeamId` usa `'AZUL'`/`'VERMELHO'`; `LegacyPlayerPayload` recebe `time?` e `papel?` |
| `PlayerLayer.ts` | `getTeamColor` mapeado para `'AZUL'`/`'VERMELHO'` |
| `normalizers.ts` | `normalizeLegacyPlayer` le `value.time`; remove `inferTeamFromId` |

**Nao sao alterados:**

- `ConfiguracaoJogador.java` — ja tem `Time` e `PapelJogador` separados do nome
- `JogadorAgent.java` — nao cria nem sobrescreve `JogadorEstado.time`/`papel`
- `ControladorDecisaoJogador.java` — usa `ContextoDecisao`, que vem do agente, nao do DTO
- `SnapshotPlayerPayload` / `normalizeSnapshotPlayer` — formato snapshot nao e afetado
- `SistemaFutebol.localizarOponenteProximoParaDisputa` — usa enum `Time` do `ConfiguracaoJogador`

---

## Invariantes

- `JogadorEstado.time` e `JogadorEstado.papel` sao imutaveis: setados em `criarEstadoInicial`,
  nunca sobrescritos pelo `JogadorAgent` ao longo da partida.
- O payload WebSocket e aditivo: campos novos nao quebram clientes que nao os leem.
- A cor do time no frontend e determinada pelo campo `time` do payload, nao pelo nome do agente.
- `papel` esta presente no JSON (serializado pelo Gson) mas ignorado pelo frontend por ora —
  disponivel para uso futuro sem nova mudanca no backend.

---

## Extensibilidade (sem implementar agora)

No futuro, `ConfiguracaoJogador` (ou uma `ConfiguracaoTime` separada) podera receber `corHex`
e `nomeExibicao` para o time. Quando isso acontecer:

- `JogadorEstado.time` poderia passar a carregar `corHex` diretamente, eliminando o
  mapeamento hardcoded em `getTeamColor`.
- `PlayerLayer.getTeamColor` seria substituido por leitura direta do campo de cor do payload.
- O identificador `"AZUL"`/`"VERMELHO"` continuaria valido como ID interno ate essa migracao.

---

## Validacao

```powershell
cd futebol-colaborativo
mvn test
```

```powershell
cd futebol-colaborativo
mvn exec:java
```

Verificar nos logs que os 4 agentes sobem com os novos nomes. Em outro terminal:

```powershell
cd frontend
npm run dev
```

Verificar no campo:

- 2 circulos azuis e 2 vermelhos — cores corretas independente do nome.
- Nomes novos dos agentes visiveis acima dos circulos.
- Nenhuma regressao no comportamento de disputa ou movimento.

## Criterios de Sucesso

- Backend compila e sobe com 4 agentes de nomes livres.
- Frontend renderiza cores corretas lendo `time` do payload, nao do nome.
- `inferTeamFromId` nao existe mais em `normalizers.ts`.
- Renomear um agente no `App.java` para qualquer string valida nao afeta a cor renderizada.
