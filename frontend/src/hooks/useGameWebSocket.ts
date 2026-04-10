import { startTransition, useEffect, useRef, useState } from 'react'

import { appendGameEvents, createGameEvent, type GameEvent } from '../game/events/gameEvents'
import { createEmptyGameState } from '../game/model/gameState'
import { normalizeGamePayload } from '../game/model/normalizers'
import type { GameState, PayloadFormat } from '../game/model/gameTypes'
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
            message: 'Conexão WebSocket estabelecida.',
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
            message: 'Erro de comunicação no WebSocket.',
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
              message: 'Mensagem recebida não é um JSON válido.',
            }),
          ])
          return
        }

        const normalized = normalizeGamePayload(parsedPayload)
        if (!normalized.ok) {
          pushEvents(normalized.events)
          return
        }

        const nextEvents = [...normalized.events]
        if (lastPayloadFormatRef.current !== normalized.state.meta.payloadFormat) {
          nextEvents.push(
            createGameEvent({
              kind: 'payload-format',
              message: `Payload ${normalized.state.meta.payloadFormat} detectado e normalizado.`,
            }),
          )
          lastPayloadFormatRef.current = normalized.state.meta.payloadFormat
        }

        startTransition(() => {
          setGameState(normalized.state)
          setConnectionStatus('connected')
        })

        pushEvents(nextEvents)
      },
    })

    lastPayloadFormatRef.current = null
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
