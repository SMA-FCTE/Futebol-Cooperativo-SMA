export type TeamId = 'A' | 'B' | 'unknown'

export type PayloadFormat = 'legacy' | 'snapshot'

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
}

export type LegacyPayload = Record<string, LegacyPlayerPayload>

export type SnapshotPlayerPayload = {
  id?: unknown
  team?: unknown
  x?: unknown
  y?: unknown
  velocidade?: unknown
  theta?: unknown
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
