export type GameEventLevel = 'info' | 'warning' | 'error'

export type GameEventKind =
  | 'socket-status'
  | 'socket-open'
  | 'socket-close'
  | 'socket-error'
  | 'payload-error'
  | 'payload-format'
  | 'ball-possession'
  | 'goal-reset'

export type GameEvent = {
  id: string
  kind: GameEventKind
  level: GameEventLevel
  message: string
  timestamp: number
}

export const MAX_GAME_EVENTS = 20

type CreateGameEventInput = {
  kind: GameEventKind
  level?: GameEventLevel
  message: string
  timestamp?: number
}

export function createGameEvent({
  kind,
  level = 'info',
  message,
  timestamp = Date.now(),
}: CreateGameEventInput): GameEvent {
  return {
    id: `event-${timestamp}-${Math.random().toString(36).slice(2, 8)}`,
    kind,
    level,
    message,
    timestamp,
  }
}

export function appendGameEvent(queue: GameEvent[], event: GameEvent): GameEvent[] {
  return [...queue.slice(-(MAX_GAME_EVENTS - 1)), event]
}

export function appendGameEvents(queue: GameEvent[], events: GameEvent[]): GameEvent[] {
  return events.reduce(appendGameEvent, queue)
}
