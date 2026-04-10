export type TeamId = 'A' | 'B' | 'unknown'

export type PayloadFormat = 'legacy-flat' | 'legacy-nested' | 'snapshot'

export type FieldState = {
  width: number
  height: number
}

export type PlayerState = {
  id: string
  team: TeamId
  x: number
  y: number
  velocidade: number
  theta: number
  comBola: boolean
}

export type BallState = {
  x: number
  y: number
  emPosseDe: string | null
}

export type Scoreboard = {
  A: number
  B: number
}

export type GameState = {
  field: FieldState
  players: PlayerState[]
  ball: BallState | null
  scoreboard: Scoreboard
  tempo: number | null
  meta: {
    payloadFormat: PayloadFormat
    updatedAt: number | null
  }
}

export type LegacyPlayerPayload = {
  x?: unknown
  y?: unknown
  velocidade?: unknown
  theta?: unknown
  comBola?: unknown
}

export type LegacyFlatPayload = Record<string, LegacyPlayerPayload>

export type LegacyNestedPlayersPayload = Record<string, LegacyPlayerPayload>

export type LegacyNestedPayload = {
  jogadores?: LegacyNestedPlayersPayload
  bola?: SnapshotBallPayload | null
}

export type SnapshotPlayerPayload = {
  id?: unknown
  team?: unknown
  x?: unknown
  y?: unknown
  velocidade?: unknown
  theta?: unknown
  comBola?: unknown
}

export type SnapshotBallPayload = {
  x?: unknown
  y?: unknown
  emPosseDe?: unknown
}

export type SnapshotScoreboardPayload = {
  A?: unknown
  B?: unknown
}

export type SnapshotPayload = {
  tempo?: unknown
  bola?: SnapshotBallPayload | null
  jogadores?: unknown
  placar?: SnapshotScoreboardPayload
}
