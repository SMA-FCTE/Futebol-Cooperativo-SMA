import { useState } from 'react'

type MatchControlPanelProps = {
  onIniciar: (duracaoSegundos: number) => void
  loading: boolean
}

function MatchControlPanel({ onIniciar, loading }: MatchControlPanelProps) {
  const [minutos, setMinutos] = useState(5)

  function handleSubmit() {
    const duracaoMinutos = Math.max(1, Math.min(30, minutos))
    onIniciar(duracaoMinutos * 60)
  }

  return (
    <div className="match-control-panel">
      <label htmlFor="duracao-partida">Duração</label>
      <input
        id="duracao-partida"
        type="number"
        min={1}
        max={30}
        value={minutos}
        onChange={(event) => setMinutos(Number(event.target.value))}
        disabled={loading}
      />
      <span className="match-control-unit">min</span>
      <button className="btn" type="button" onClick={handleSubmit} disabled={loading}>
        {loading ? 'Iniciando...' : 'Iniciar Partida'}
      </button>
    </div>
  )
}

export default MatchControlPanel
