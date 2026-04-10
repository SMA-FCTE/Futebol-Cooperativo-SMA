import type { BallState } from '../../game/model/gameTypes'
import type { ConnectionStatus } from '../../services/websocket/gameSocket'

type ScoreboardProps = {
  connectionStatus: ConnectionStatus
  tempo: number | null
  playerCount: number
  ball: BallState | null
  ballCarrierId: string | null
}

const connectionLabels: Record<ConnectionStatus, string> = {
  idle: 'Parado',
  connecting: 'Conectando',
  connected: 'Ao vivo',
  reconnecting: 'Reconectando',
  disconnected: 'Desconectado',
  error: 'Erro',
}

function Scoreboard({
  connectionStatus,
  playerCount,
  ball,
  ballCarrierId,
}: ScoreboardProps) {
  const ballStateLabel = ballCarrierId
    ? 'Em conducao'
    : ball
      ? 'Solta no campo'
      : 'Sem leitura'

  return (
    <section className="panel scoreboard-panel">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Painel React</span>
          <h2>Estado da jogada</h2>
        </div>
        <span className={`status-pill is-${connectionStatus}`}>
          {connectionLabels[connectionStatus]}
        </span>
      </div>

      <div className="scoreboard-grid">
        <article className="team-card team-a">
          <span className="team-label">Posse atual</span>
          <strong className="score-value score-value-compact">
            {ballCarrierId ?? 'Ninguem'}
          </strong>
        </article>
        <article className="team-card team-b">
          <span className="team-label">Estado da bola</span>
          <strong className="score-value score-value-compact">{ballStateLabel}</strong>
        </article>
      </div>

      <div className="meta-strip">
        <div className="metric-card">
          <span className="metric-label">Jogadores</span>
          <strong className="metric-value">{playerCount}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Posicao da bola</span>
          <strong className="metric-value">{formatBallPosition(ball)}</strong>
        </div>
      </div>
    </section>
  )
}

function formatBallPosition(ball: BallState | null): string {
  if (!ball) {
    return 'Sem dado'
  }

  return `${ball.x}, ${ball.y}`
}

export default Scoreboard
