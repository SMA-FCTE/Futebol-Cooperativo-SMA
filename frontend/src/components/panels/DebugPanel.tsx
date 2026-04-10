import { findBallCarrier } from '../../game/model/gameState'
import type { GameEvent } from '../../game/events/gameEvents'
import type { GameState } from '../../game/model/gameTypes'
import type { ConnectionStatus } from '../../services/websocket/gameSocket'

type DebugPanelProps = {
  connectionStatus: ConnectionStatus
  gameState: GameState
  lastRawMessage: string | null
  events: GameEvent[]
  httpStatus: string | null
  httpPlayersCount: number | null
  httpError: string | null
  wsUrl: string
  apiUrl: string
}

function DebugPanel({
  connectionStatus,
  gameState,
  lastRawMessage,
  events,
  httpStatus,
  httpPlayersCount,
  httpError,
  wsUrl,
  apiUrl,
}: DebugPanelProps) {
  const recentEvents = [...events].reverse()
  const ballCarrier = findBallCarrier(gameState.players)
  const playersWithBall = gameState.players.filter((player) => player.comBola)

  return (
    <section className="panel debug-panel">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Diagnostico</span>
          <h2>Payloads, posse e bootstrap HTTP</h2>
        </div>
      </div>

      <div className="debug-block">
        <h3>Resumo da sessao</h3>
        <ul className="debug-list">
          <li>
            <span>Status WS</span>
            <strong>{connectionStatus}</strong>
          </li>
          <li>
            <span>Formato detectado</span>
            <strong>{gameState.meta.payloadFormat}</strong>
          </li>
          <li>
            <span>Ultima atualizacao</span>
            <strong>{formatTimestamp(gameState.meta.updatedAt)}</strong>
          </li>
          <li>
            <span>Endpoint WS</span>
            <strong>{wsUrl}</strong>
          </li>
          <li>
            <span>Endpoint API</span>
            <strong>{apiUrl}</strong>
          </li>
        </ul>
      </div>

      <div className="debug-block">
        <h3>Estado da jogada</h3>
        <ul className="debug-list">
          <li>
            <span>Portador da bola</span>
            <strong>{ballCarrier?.id ?? 'Ninguem'}</strong>
          </li>
          <li>
            <span>Jogadores com posse</span>
            <strong>{playersWithBall.length > 0 ? playersWithBall.map((player) => player.id).join(', ') : 'Nenhum'}</strong>
          </li>
          <li>
            <span>Posicao da bola</span>
            <strong>{formatBallPosition(gameState)}</strong>
          </li>
          <li>
            <span>Total de jogadores</span>
            <strong>{gameState.players.length}</strong>
          </li>
        </ul>
      </div>

      <div className="debug-block">
        <h3>Bootstrap HTTP</h3>
        <ul className="debug-list">
          <li>
            <span>/api/status</span>
            <strong>{httpStatus ?? 'Aguardando resposta'}</strong>
          </li>
          <li>
            <span>/api/jogadores</span>
            <strong>
              {httpPlayersCount === null ? 'Aguardando resposta' : `${httpPlayersCount} registros`}
            </strong>
          </li>
          <li>
            <span>Erro HTTP</span>
            <strong>{httpError ?? 'Nenhum'}</strong>
          </li>
        </ul>
      </div>

      <div className="debug-block">
        <h3>Ultimo payload cru</h3>
        <pre className="payload-viewer">
          {lastRawMessage ?? 'Nenhuma mensagem recebida ainda.'}
        </pre>
      </div>

      <div className="debug-block">
        <h3>Eventos recentes</h3>
        {recentEvents.length === 0 ? (
          <p className="empty-state">Nenhum evento efemero registrado ainda.</p>
        ) : (
          <ol className="event-list">
            {recentEvents.map((event) => (
              <li key={event.id} className={`event-item is-${event.level}`}>
                <div className="event-header">
                  <span className="event-kind">{event.kind}</span>
                  <time>{formatTimestamp(event.timestamp)}</time>
                </div>
                <p className="event-message">{event.message}</p>
              </li>
            ))}
          </ol>
        )}
      </div>
    </section>
  )
}

function formatTimestamp(timestamp: number | null): string {
  if (!timestamp) {
    return 'Sem dado'
  }

  return new Date(timestamp).toLocaleTimeString('pt-BR')
}

function formatBallPosition(gameState: GameState): string {
  if (!gameState.ball) {
    return 'Sem dado'
  }

  return `${gameState.ball.x}, ${gameState.ball.y}`
}

export default DebugPanel
