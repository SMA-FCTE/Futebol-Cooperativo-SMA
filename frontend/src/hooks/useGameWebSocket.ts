import { startTransition, useEffect, useRef, useState } from 'react'

import { appendGameEvents, createGameEvent, type GameEvent } from '../game/events/gameEvents'
import {
  createEmptyGameState,
  DEFAULT_PLAYER_ORIGIN,
  findBallCarrier,
  isBallAtCenter,
} from '../game/model/gameState'
import { normalizeGamePayload } from '../game/model/normalizers'
import type { GameState, PayloadFormat, PlayerState } from '../game/model/gameTypes'
import { GameSocket, type ConnectionStatus } from '../services/websocket/gameSocket'

type UseGameWebSocketResult = {
  gameState: GameState
  connectionStatus: ConnectionStatus
  lastRawMessage: string | null
  events: GameEvent[]
}

export function useGameWebSocket(url: string): UseGameWebSocketResult {
  const [gameState, setGameState] = useState(createEmptyGameState)
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>('idle')
  const [lastRawMessage, setLastRawMessage] = useState<string | null>(null)
  const [events, setEvents] = useState<GameEvent[]>([])
  const lastPayloadFormatRef = useRef<PayloadFormat | null>(null)
  const lastGameStateRef = useRef<GameState | null>(null)

  useEffect(() => {
    let isActive = true

    const pushEvents = (nextEvents: GameEvent[]) => {
      if (!isActive || nextEvents.length === 0) {
        return
      }

      startTransition(() => {
        setEvents((currentEvents) => appendGameEvents(currentEvents, nextEvents))
      })
    }

    const socket = new GameSocket(url, {
      onOpen: () => {
        if (!isActive) {
          return
        }

        startTransition(() => {
          setConnectionStatus('connected')
        })

        pushEvents([
          createGameEvent({
            kind: 'socket-open',
            message: 'Conexao WebSocket estabelecida.',
          }),
        ])
      },
      onClose: (info) => {
        if (!isActive) {
          return
        }

        const nextStatus: ConnectionStatus = info.willReconnect
          ? 'reconnecting'
          : 'disconnected'
        const reason = info.reason ? ` Motivo: ${info.reason}.` : ''
        const retry =
          info.willReconnect && info.nextDelayMs !== null
            ? ` Nova tentativa em ${Math.round(info.nextDelayMs / 1000)}s.`
            : ''

        startTransition(() => {
          setConnectionStatus(nextStatus)
        })

        pushEvents([
          createGameEvent({
            kind: 'socket-close',
            level: info.willReconnect ? 'warning' : 'info',
            message: `Socket fechado (code ${info.code}).${reason}${retry}`,
          }),
        ])
      },
      onError: () => {
        if (!isActive) {
          return
        }

        startTransition(() => {
          setConnectionStatus('error')
        })

        pushEvents([
          createGameEvent({
            kind: 'socket-error',
            level: 'error',
            message: 'Erro de comunicacao no WebSocket.',
          }),
        ])
      },
      onMessage: (raw) => {
        if (!isActive) {
          return
        }

        let parsedPayload: unknown

        startTransition(() => {
          setLastRawMessage(raw)
        })

        try {
          parsedPayload = JSON.parse(raw)
        } catch {
          pushEvents([
            createGameEvent({
              kind: 'payload-error',
              level: 'error',
              message: 'Mensagem recebida nao e um JSON valido.',
            }),
          ])
          return
        }

        const normalized = normalizeGamePayload(parsedPayload)
        if (!normalized.ok) {
          pushEvents(normalized.events)
          return
        }

        const previousState = lastGameStateRef.current
        const nextState = normalized.state
        const nextEvents = [...normalized.events]

        if (lastPayloadFormatRef.current !== nextState.meta.payloadFormat) {
          nextEvents.push(
            createGameEvent({
              kind: 'payload-format',
              message: `Payload ${nextState.meta.payloadFormat} detectado e normalizado.`,
            }),
          )
          lastPayloadFormatRef.current = nextState.meta.payloadFormat
        }

        if (previousState) {
          nextEvents.push(...inferCycleEvents(previousState, nextState))
        }

        lastGameStateRef.current = nextState

        startTransition(() => {
          setGameState(nextState)
          setConnectionStatus('connected')
        })

        pushEvents(nextEvents)
      },
    })

    lastPayloadFormatRef.current = null
    lastGameStateRef.current = null
    startTransition(() => {
      setConnectionStatus('connecting')
    })
    pushEvents([
      createGameEvent({
        kind: 'socket-status',
        message: `Conectando ao WebSocket em ${url}.`,
      }),
    ])

    socket.connect()

    return () => {
      isActive = false
      socket.disconnect()
    }
  }, [url])

  return {
    gameState,
    connectionStatus,
    lastRawMessage,
    events,
  }
}

function inferCycleEvents(previousState: GameState, nextState: GameState): GameEvent[] {
  const previousCarrier = findBallCarrier(previousState.players)
  const nextCarrier = findBallCarrier(nextState.players)
  const nextEvents: GameEvent[] = []

  if (previousCarrier?.id !== nextCarrier?.id) {
    if (!previousCarrier && nextCarrier) {
      nextEvents.push(
        createGameEvent({
          kind: 'ball-possession',
          message: `${nextCarrier.id} pegou a bola.`,
        }),
      )
    } else if (previousCarrier && nextCarrier) {
      nextEvents.push(
        createGameEvent({
          kind: 'ball-possession',
          level: 'warning',
          message: `Posse mudou de ${previousCarrier.id} para ${nextCarrier.id}.`,
        }),
      )
    } else if (previousCarrier && !nextCarrier) {
      nextEvents.push(
        createGameEvent({
          kind: 'ball-possession',
          level: 'warning',
          message: `A bola ficou sem posse apos sair de ${previousCarrier.id}.`,
        }),
      )
    }
  }

  if (isLikelyGoalReset(previousState, nextState, previousCarrier, nextCarrier)) {
    nextEvents.push(
      createGameEvent({
        kind: 'goal-reset',
        level: 'warning',
        message: 'Reset de jogada detectado: bola voltou ao centro apos uma conducao.',
      }),
    )
  }

  return nextEvents
}

function isLikelyGoalReset(
  previousState: GameState,
  nextState: GameState,
  previousCarrier: PlayerState | null,
  nextCarrier: PlayerState | null,
): boolean {
  if (!previousCarrier || nextCarrier) {
    return false
  }

  if (!isBallAtCenter(nextState)) {
    return false
  }

  const resetPlayer = nextState.players.find((player) => player.id === previousCarrier.id)
  if (!resetPlayer) {
    return false
  }

  const returnedToOrigin =
    resetPlayer.x === DEFAULT_PLAYER_ORIGIN.x && resetPlayer.y === DEFAULT_PLAYER_ORIGIN.y

  const ballWasBeingCarried = previousState.ball?.emPosseDe === previousCarrier.id

  return returnedToOrigin && ballWasBeingCarried
}
