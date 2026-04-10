export type ConnectionStatus =
  | 'idle'
  | 'connecting'
  | 'connected'
  | 'reconnecting'
  | 'disconnected'
  | 'error'

export type GameSocketCloseInfo = {
  code: number
  reason: string
  wasClean: boolean
  willReconnect: boolean
  reconnectAttempt: number
  nextDelayMs: number | null
}

type GameSocketHandlers = {
  onMessage?: (raw: string) => void
  onOpen?: () => void
  onClose?: (info: GameSocketCloseInfo) => void
  onError?: (event: Event) => void
}

const RECONNECT_DELAYS_MS = [1000, 2000, 5000] as const

export class GameSocket {
  private socket: WebSocket | null = null

  private reconnectTimer: number | null = null

  private reconnectAttempt = 0

  private manuallyClosed = false

  private readonly url: string

  private readonly handlers: GameSocketHandlers

  constructor(url: string, handlers: GameSocketHandlers = {}) {
    this.url = url
    this.handlers = handlers
  }

  connect(): void {
    this.manuallyClosed = false
    this.clearReconnectTimer()
    this.connectInternal()
  }

  disconnect(): void {
    this.manuallyClosed = true
    this.clearReconnectTimer()

    const activeSocket = this.socket
    this.socket = null
    activeSocket?.close()
  }

  private connectInternal(): void {
    if (
      this.socket &&
      (this.socket.readyState === WebSocket.OPEN ||
        this.socket.readyState === WebSocket.CONNECTING)
    ) {
      return
    }

    const socket = new WebSocket(this.url)
    this.socket = socket

    socket.onopen = () => {
      this.reconnectAttempt = 0
      this.handlers.onOpen?.()
    }

    socket.onmessage = (event) => {
      this.handlers.onMessage?.(
        typeof event.data === 'string' ? event.data : String(event.data),
      )
    }

    socket.onerror = (event) => {
      this.handlers.onError?.(event)
    }

    socket.onclose = (event) => {
      if (this.socket === socket) {
        this.socket = null
      }

      const willReconnect = !this.manuallyClosed
      const nextAttempt = willReconnect ? this.reconnectAttempt + 1 : this.reconnectAttempt
      const nextDelayMs = willReconnect ? getReconnectDelay(nextAttempt) : null

      this.handlers.onClose?.({
        code: event.code,
        reason: event.reason,
        wasClean: event.wasClean,
        willReconnect,
        reconnectAttempt: nextAttempt,
        nextDelayMs,
      })

      if (willReconnect && nextDelayMs !== null) {
        this.scheduleReconnect(nextAttempt, nextDelayMs)
      }
    }
  }

  private scheduleReconnect(attempt: number, delayMs: number): void {
    this.reconnectAttempt = attempt
    this.clearReconnectTimer()
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null
      this.connectInternal()
    }, delayMs)
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer === null) {
      return
    }

    window.clearTimeout(this.reconnectTimer)
    this.reconnectTimer = null
  }
}

function getReconnectDelay(attempt: number): number {
  return RECONNECT_DELAYS_MS[Math.min(attempt - 1, RECONNECT_DELAYS_MS.length - 1)]
}
