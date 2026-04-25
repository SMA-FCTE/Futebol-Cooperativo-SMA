import { findBallCarrier } from '../../game/model/gameState'
import type { GameState, PlayerState } from '../../game/model/gameTypes'

type MatchStatePanelProps = {
  state: GameState
}

function MatchStatePanel({ state }: MatchStatePanelProps) {
  const vermelho = state.players.find((player) => player.id === 'vermelho') ?? null
  const azul = state.players.find((player) => player.id === 'azul') ?? null
  const carrier = findBallCarrier(state.players)
  const disputa = state.disputa

  return (
    <div className="match-state-panel" aria-label="Estado atual da partida">
      <PlayerStateCard title="Jogador vermelho" player={vermelho} />
      <PlayerStateCard title="Jogador azul" player={azul} />

      <article className="match-state-card">
        <h3>Estado da bola</h3>
        <StateList
          rows={[
            ['Posicao', state.ball ? formatPosition(state.ball.x, state.ball.y) : 'Sem dados'],
            ['Portador', carrier?.id ?? state.ball?.emPosseDe ?? 'Ninguem'],
          ]}
        />
      </article>

      <article className="match-state-card">
        <h3>Ultima disputa</h3>
        <StateList
          rows={[
            ['ID', disputa?.id ?? 'Sem disputa'],
            ['Rodada', disputa ? String(disputa.rodada) : '-'],
            ['Jogadas', disputa ? `${disputa.jogada1 ?? '?'} x ${disputa.jogada2 ?? '?'}` : 'Sem jogadas'],
            ['Vencedor', disputa?.vencedor ?? (disputa?.empate ? 'Empate' : 'Indefinido')],
            ['Empate', disputa ? formatBoolean(disputa.empate) : '-'],
            ['Resultado', disputa?.resultado ?? 'Aguardando disputa'],
          ]}
        />
      </article>
    </div>
  )
}

function PlayerStateCard({ title, player }: { title: string; player: PlayerState | null }) {
  return (
    <article className="match-state-card">
      <h3>{title}</h3>
      <StateList
        rows={[
          ['Posicao', player ? formatPosition(player.x, player.y) : 'Sem dados'],
          ['Velocidade', player ? formatNumber(player.velocidade) : 'Sem dados'],
          ['Com bola', player ? formatBoolean(player.comBola) : 'Sem dados'],
          ['Gol alvo', player?.golX === null || player === null ? 'Sem dados' : formatNumber(player.golX)],
        ]}
      />
    </article>
  )
}

function StateList({ rows }: { rows: Array<[string, string]> }) {
  return (
    <dl className="match-state-list">
      {rows.map(([label, value]) => (
        <div key={label}>
          <dt>{label}</dt>
          <dd>{value}</dd>
        </div>
      ))}
    </dl>
  )
}

function formatPosition(x: number, y: number): string {
  return `${formatNumber(x)}, ${formatNumber(y)}`
}

function formatNumber(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(2)
}

function formatBoolean(value: boolean): string {
  return value ? 'Sim' : 'Nao'
}

export default MatchStatePanel
