import { createGameEvent, type GameEvent } from '../events/gameEvents'
import {
  createEmptyGameState,
  DEFAULT_SCOREBOARD,
  findBallCarrier,
  sortPlayers,
} from './gameState'
import type {
  BallState,
  DisputaBolaState,
  GameState,
  JogadaDisputa,
  LegacyFlatPayload,
  LegacyNestedPayload,
  PasseState,
  PayloadFormat,
  PlayerState,
  SnapshotPayload,
  TeamId,
} from './gameTypes'

type NormalizationSuccess = {
  ok: true
  state: GameState
  events: GameEvent[]
}

type NormalizationFailure = {
  ok: false
  reason: string
  events: GameEvent[]
}

export type NormalizationResult = NormalizationSuccess | NormalizationFailure

export function normalizeGamePayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload root precisa ser um objeto JSON.')
  }

  const payloadFormat = detectPayloadFormat(raw)
  switch (payloadFormat) {
    case 'legacy-nested':
      return normalizeLegacyNestedPayload(raw)
    case 'snapshot':
      return normalizeSnapshotPayload(raw)
    default:
      return normalizeLegacyFlatPayload(raw)
  }
}

export function normalizeLegacyFlatPayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload legado plano invalido.')
  }

  const players = Object.entries(raw as LegacyFlatPayload).flatMap(([id, value]) => {
    if (id === 'bola' || id === 'jogadores' || id === 'disputa' || id === 'passe') {
      return []
    }

    const player = normalizeLegacyPlayer(id, value)
    return player ? [player] : []
  })

  if (players.length === 0) {
    return createFailure('Payload legado plano sem jogadores validos.')
  }

  return createSuccess({
    payloadFormat: 'legacy-flat',
    players,
    ball: null,
    disputa: normalizeDisputa((raw as { disputa?: unknown }).disputa),
    passe: normalizePasse((raw as { passe?: unknown }).passe),
    tempo: null,
    scoreboard: { ...DEFAULT_SCOREBOARD },
  })
}

export function normalizeLegacyNestedPayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload legado aninhado invalido.')
  }

  const payload = raw as LegacyNestedPayload
  if (!isRecord(payload.jogadores)) {
    return createFailure('Payload legado aninhado sem mapa de jogadores valido.')
  }

  const players = Object.entries(payload.jogadores).flatMap(([id, value]) => {
    const player = normalizeLegacyPlayer(id, value)
    return player ? [player] : []
  })

  if (players.length === 0) {
    return createFailure('Payload legado aninhado sem jogadores validos.')
  }

  const ball = normalizeBall(payload.bola, inferPossessorId(players))
  const disputa = normalizeDisputa(payload.disputa)
  const passe = normalizePasse(payload.passe)

  return createSuccess({
    payloadFormat: 'legacy-nested',
    players,
    ball,
    disputa,
    passe,
    tempo: null,
    scoreboard: { ...DEFAULT_SCOREBOARD },
  })
}

export function normalizeSnapshotPayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload snapshot invalido.')
  }

  const payload = raw as SnapshotPayload
  const players = Array.isArray(payload.jogadores)
    ? payload.jogadores.flatMap((player) => {
        const normalizedPlayer = normalizeSnapshotPlayer(player)
        return normalizedPlayer ? [normalizedPlayer] : []
      })
    : []

  const normalizedBall = normalizeBall(payload.bola, null)
  const possessorId = normalizedBall?.emPosseDe ?? inferPossessorId(players)
  const playersWithPossession = players.map((player) =>
    possessorId && player.id === possessorId ? { ...player, comBola: true } : player,
  )

  return createSuccess({
    payloadFormat: 'snapshot',
    players: playersWithPossession,
    ball: normalizedBall
      ? {
          ...normalizedBall,
          emPosseDe: possessorId,
        }
      : null,
    disputa: normalizeDisputa(payload.disputa),
    passe: normalizePasse(payload.passe),
    tempo: coerceNullableNumber(payload.tempo),
    scoreboard: normalizeScoreboard(payload.placar),
  })
}

function createSuccess({
  payloadFormat,
  players,
  ball,
  disputa,
  passe,
  tempo,
  scoreboard,
}: {
  payloadFormat: PayloadFormat
  players: PlayerState[]
  ball: BallState | null
  disputa: DisputaBolaState | null
  passe: PasseState | null
  tempo: number | null
  scoreboard: { A: number; B: number }
}): NormalizationSuccess {
  const baseState = createEmptyGameState()
  const sortedPlayers = sortPlayers(players)
  const possessorId = ball?.emPosseDe ?? inferPossessorId(sortedPlayers)
  const normalizedPlayers = sortedPlayers.map((player) => ({
    ...player,
    comBola: possessorId ? player.id === possessorId : player.comBola,
  }))

  return {
    ok: true,
    state: {
      ...baseState,
      players: normalizedPlayers,
      ball: ball
        ? {
            ...ball,
            emPosseDe: possessorId,
          }
        : null,
      disputa,
      passe,
      tempo,
      scoreboard,
      meta: {
        payloadFormat,
        updatedAt: Date.now(),
      },
    },
    events: [],
  }
}

function detectPayloadFormat(raw: Record<string, unknown>): PayloadFormat {
  if (Array.isArray(raw.jogadores)) {
    return 'snapshot'
  }

  if (isRecord(raw.jogadores)) {
    return 'legacy-nested'
  }

  return 'legacy-flat'
}

function normalizeLegacyPlayer(id: string, value: unknown): PlayerState | null {
  if (!isRecord(value)) {
    return null
  }

  const x = coerceNullableNumber(value.x)
  const y = coerceNullableNumber(value.y)

  if (x === null || y === null) {
    return null
  }

  return {
    id,
    team: normalizeTeam(value.time),
    x,
    y,
    velocidade: coerceNullableNumber(value.velocidade) ?? 0,
    theta: coerceNullableNumber(value.theta) ?? 0,
    comBola: typeof value.comBola === 'boolean' ? value.comBola : false,
    golX: coerceNullableNumber(value.golX),
  }
}

function normalizeSnapshotPlayer(value: unknown): PlayerState | null {
  if (!isRecord(value)) {
    return null
  }

  const id = typeof value.id === 'string' && value.id.trim().length > 0 ? value.id : null
  const x = coerceNullableNumber(value.x)
  const y = coerceNullableNumber(value.y)

  if (!id || x === null || y === null) {
    return null
  }

  return {
    id,
    team: normalizeTeam(value.team),
    x,
    y,
    velocidade: coerceNullableNumber(value.velocidade) ?? 0,
    theta: coerceNullableNumber(value.theta) ?? 0,
    comBola: typeof value.comBola === 'boolean' ? value.comBola : false,
    golX: coerceNullableNumber(value.golX),
  }
}

function normalizeBall(value: unknown, fallbackPossessorId: string | null): BallState | null {
  if (value === null || value === undefined) {
    return null
  }

  if (!isRecord(value)) {
    return null
  }

  const x = coerceNullableNumber(value.x)
  const y = coerceNullableNumber(value.y)

  if (x === null || y === null) {
    return null
  }

  return {
    x,
    y,
    emPosseDe:
      typeof value.emPosseDe === 'string' && value.emPosseDe.trim().length > 0
        ? value.emPosseDe
        : fallbackPossessorId,
  }
}

function normalizeDisputa(value: unknown): DisputaBolaState | null {
  if (value === null || value === undefined) {
    return null
  }

  if (!isRecord(value)) {
    return null
  }

  const id = coerceNonEmptyString(value.id)
  const rodada = coerceNullableNumber(value.rodada)
  const jogador1 = coerceNonEmptyString(value.jogador1)
  const jogador2 = coerceNonEmptyString(value.jogador2)

  if (!id || rodada === null || !jogador1 || !jogador2) {
    return null
  }

  return {
    id,
    rodada,
    jogador1,
    jogador2,
    jogada1: normalizeJogadaDisputa(value.jogada1),
    jogada2: normalizeJogadaDisputa(value.jogada2),
    vencedor: coerceNonEmptyString(value.vencedor),
    empate: typeof value.empate === 'boolean' ? value.empate : false,
    resultado: coerceNonEmptyString(value.resultado) ?? 'Disputa em andamento',
  }
}

function normalizePasse(value: unknown): PasseState | null {
  if (!isRecord(value)) {
    return null
  }

  const id = coerceNonEmptyString(value.id)
  const passador = coerceNonEmptyString(value.passador)
  const receptor = coerceNonEmptyString(value.receptor)
  const iniciador = coerceNonEmptyString(value.iniciador)
  const status = coerceNonEmptyString(value.status)

  if (!id || !passador || !receptor || !iniciador || !status) {
    return null
  }

  return {
    id,
    passador,
    receptor,
    iniciador,
    status,
    forcaX: coerceNullableNumber(value.forcaX),
    forcaY: coerceNullableNumber(value.forcaY),
    recebido: typeof value.recebido === 'boolean' ? value.recebido : false,
    motivoRecusa: coerceNonEmptyString(value.motivoRecusa),
    resultado: coerceNonEmptyString(value.resultado) ?? 'Passe em andamento',
  }
}

function normalizeJogadaDisputa(value: unknown): JogadaDisputa | null {
  return value === 'PEDRA' || value === 'PAPEL' || value === 'TESOURA' ? value : null
}

function normalizeScoreboard(value: unknown) {
  if (!isRecord(value)) {
    return { ...DEFAULT_SCOREBOARD }
  }

  return {
    A: coerceNullableNumber(value.A) ?? DEFAULT_SCOREBOARD.A,
    B: coerceNullableNumber(value.B) ?? DEFAULT_SCOREBOARD.B,
  }
}

function normalizeTeam(value: unknown): TeamId {
  return value === 'AZUL' || value === 'VERMELHO' ? value : 'unknown'
}

function inferPossessorId(players: PlayerState[]): string | null {
  return findBallCarrier(players)?.id ?? null
}

function coerceNullableNumber(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null
}

function coerceNonEmptyString(value: unknown): string | null {
  return typeof value === 'string' && value.trim().length > 0 ? value : null
}

function createFailure(reason: string): NormalizationFailure {
  return {
    ok: false,
    reason,
    events: [
      createGameEvent({
        kind: 'payload-error',
        level: 'error',
        message: reason,
      }),
    ],
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
