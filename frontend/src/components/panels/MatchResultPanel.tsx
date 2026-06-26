type MatchResultPanelProps = {
  scoreA: number
  scoreB: number
  onReiniciar: () => void
  loading: boolean
}

function MatchResultPanel({ scoreA, scoreB, onReiniciar, loading }: MatchResultPanelProps) {
  const label =
    scoreA > scoreB ? 'Azul venceu!' : scoreB > scoreA ? 'Vermelho venceu!' : 'Empate!'

  const labelClass =
    scoreA > scoreB
      ? 'match-result-label match-result-label--azul'
      : scoreB > scoreA
        ? 'match-result-label match-result-label--vermelho'
        : 'match-result-label'

  return (
    <div className="match-result-panel">
      <span className={labelClass}>{label}</span>
      <div className="match-score">
        <span className="match-score-team match-score-azul">Azul</span>
        <strong className="match-score-value">
          {scoreA} &times; {scoreB}
        </strong>
        <span className="match-score-team match-score-vermelho">Vermelho</span>
      </div>
      <button className="btn" type="button" onClick={onReiniciar} disabled={loading}>
        {loading ? 'Reiniciando...' : 'Nova Partida'}
      </button>
    </div>
  )
}

export default MatchResultPanel
