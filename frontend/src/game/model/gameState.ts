import type { GameState, PlayerState, Scoreboard } from './gameTypes'

export const DEFAULT_FIELD_DIMENSIONS = {
  width: 101,
  height: 61,
} as const

export const DEFAULT_SCOREBOARD: Scoreboard = {
  A: 0,
  B: 0,
}

export function createEmptyGameState(): GameState {
  return {
    field: { ...DEFAULT_FIELD_DIMENSIONS },
    players: [],
    ball: null,
    scoreboard: { ...DEFAULT_SCOREBOARD },
    tempo: null,
    meta: {
      payloadFormat: 'legacy',
      updatedAt: null,
    },
  }
}

export function sortPlayers(players: PlayerState[]): PlayerState[] {
  return [...players].sort((left, right) => left.id.localeCompare(right.id))
}
