import type { GameState, PlayerState, Scoreboard } from './gameTypes'

export const DEFAULT_FIELD_DIMENSIONS = {
  width: 300,
  height: 150,
} as const

export const DEFAULT_SCOREBOARD: Scoreboard = {
  A: 0,
  B: 0,
}

export const DEFAULT_PLAYER_ORIGIN = {
  x: 10,
  y: 10,
} as const

export function createEmptyGameState(): GameState {
  return {
    field: { ...DEFAULT_FIELD_DIMENSIONS },
    players: [],
    ball: null,
    disputa: null,
    passe: null,
    scoreboard: { ...DEFAULT_SCOREBOARD },
    tempo: null,
    partida: null,
    meta: {
      payloadFormat: 'legacy-flat',
      updatedAt: null,
    },
  }
}

export function sortPlayers(players: PlayerState[]): PlayerState[] {
  return [...players].sort((left, right) => left.id.localeCompare(right.id))
}

export function findBallCarrier(players: PlayerState[]): PlayerState | null {
  return players.find((player) => player.comBola) ?? null
}

export function isBallAtCenter(state: GameState): boolean {
  if (!state.ball) {
    return false
  }

  return state.ball.x === state.field.width / 2 && state.ball.y === state.field.height / 2
}

export function hasPlayerAtOrigin(state: GameState): boolean {
  return state.players.some(
    (player) => player.x === DEFAULT_PLAYER_ORIGIN.x && player.y === DEFAULT_PLAYER_ORIGIN.y,
  )
}
