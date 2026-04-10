import type { Scoreboard as ScoreboardState } from '../../game/model/gameTypes'
import type { ConnectionStatus } from '../../services/websocket/gameSocket'

type ScoreboardProps = {
  connectionStatus: ConnectionStatus
  scoreboard: ScoreboardState
  tempo: number | null
  playerCount: number
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
  scoreboard,
  tempo,
  playerCount,
}: ScoreboardProps) {
  return (
    <section className="panel scoreboard-panel">
      <div className="panel-heading">
        <div>
          <span className="panel-kicker">Painel React</span>
          <h2>Placar e estado de sessão</h2>
        </div>
        <span className={`status-pill is-${connectionStatus}`}>
          {connectionLabels[connectionStatus]}
        </span>
      </div>

      <div className="scoreboard-grid">
        <article className="team-card team-a">
          <span className="team-label">Time A</span>
          <strong className="score-value">{scoreboard.A}</strong>
        </article>
        <article className="team-card team-b">
          <span className="team-label">Time B</span>
          <strong className="score-value">{scoreboard.B}</strong>
        </article>
      </div>

      <div className="meta-strip">
        <div className="metric-card">
          <span className="metric-label">Tempo</span>
          <strong className="metric-value">{formatTempo(tempo)}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Jogadores</span>
          <strong className="metric-value">{playerCount}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Fonte</span>
          <strong className="metric-value">Servidor</strong>
        </div>
      </div>
    </section>
  )
}

function formatTempo(tempo: number | null): string {
  if (tempo === null) {
    return 'Aguardando'
  }

  const totalSeconds = Math.max(0, Math.floor(tempo))
  const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, '0')
  const seconds = String(totalSeconds % 60).padStart(2, '0')

  return `${minutes}:${seconds}`
}

export default Scoreboard
