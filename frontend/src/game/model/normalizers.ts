import { createGameEvent, type GameEvent } from '../events/gameEvents'
import { createEmptyGameState, DEFAULT_SCOREBOARD, sortPlayers } from './gameState'
import type {
  BallState,
  GameState,
  LegacyPayload,
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

  if (looksLikeSnapshot(raw)) {
    return normalizeSnapshotPayload(raw)
  }

  return normalizeLegacyPayload(raw)
}

export function normalizeLegacyPayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload legado inválido.')
  }

  const players = Object.entries(raw as LegacyPayload).flatMap(([id, value]) => {
    const player = normalizeLegacyPlayer(id, value)
    return player ? [player] : []
  })

  if (players.length === 0) {
    return createFailure('Payload legado sem jogadores válidos.')
  }

  const baseState = createEmptyGameState()
  const updatedAt = Date.now()

  return {
    ok: true,
    state: {
      ...baseState,
      players: sortPlayers(players),
      meta: {
        payloadFormat: 'legacy',
        updatedAt,
      },
    },
    events: [],
  }
}

export function normalizeSnapshotPayload(raw: unknown): NormalizationResult {
  if (!isRecord(raw)) {
    return createFailure('Payload snapshot inválido.')
  }

  const payload = raw as SnapshotPayload
  const players = Array.isArray(payload.jogadores)
    ? payload.jogadores.flatMap((player) => {
        const normalizedPlayer = normalizeSnapshotPlayer(player)
        return normalizedPlayer ? [normalizedPlayer] : []
      })
    : []

  const baseState = createEmptyGameState()
  const updatedAt = Date.now()

  return {
    ok: true,
    state: {
      ...baseState,
      players: sortPlayers(players),
      ball: normalizeBall(payload.bola),
      tempo: coerceNullableNumber(payload.tempo),
      scoreboard: normalizeScoreboard(payload.placar),
      meta: {
        payloadFormat: 'snapshot',
        updatedAt,
      },
    },
    events: [],
  }
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
    team: 'unknown',
    x,
    y,
    velocidade: coerceNullableNumber(value.velocidade) ?? 0,
    theta: coerceNullableNumber(value.theta) ?? 0,
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
  }
}

function normalizeBall(value: unknown): BallState | null {
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
    emPosseDe: typeof value.emPosseDe === 'string' ? value.emPosseDe : null,
  }
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
  return value === 'A' || value === 'B' ? value : 'unknown'
}

function looksLikeSnapshot(raw: Record<string, unknown>): boolean {
  return ['tempo', 'bola', 'jogadores', 'placar'].some((key) => key in raw)
}

function coerceNullableNumber(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null
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
