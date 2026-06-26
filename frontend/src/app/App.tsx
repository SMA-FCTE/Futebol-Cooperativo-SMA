import { startTransition, useEffect, useState } from 'react'

import GameViewport from '../components/game/GameViewport'
import DebugPanel from '../components/panels/DebugPanel'
import MatchStatePanel from '../components/panels/MatchStatePanel'
import Scoreboard from '../components/panels/Scoreboard'
import { env } from '../config/env'
import { findBallCarrier } from '../game/model/gameState'
import { useGameWebSocket } from '../hooks/useGameWebSocket'
import { gameApi, type BackendPlayersResponse } from '../services/api/gameApi'

type HttpBootstrapState = {
  status: string | null
  players: BackendPlayersResponse | null
  error: string | null
}

const initialHttpState: HttpBootstrapState = {
  status: null,
  players: null,
  error: null,
}

function App() {
  const { gameState, connectionStatus, lastRawMessage, events } = useGameWebSocket(env.wsUrl)
  const [httpState, setHttpState] = useState(initialHttpState)
  const ballCarrier = findBallCarrier(gameState.players)

  useEffect(() => {
    const abortController = new AbortController()
    let isActive = true

    async function bootstrapFromHttp() {
      try {
        const [status, players] = await Promise.all([
          gameApi.getStatus({ signal: abortController.signal }),
          gameApi.getPlayers({ signal: abortController.signal }),
        ])

        if (!isActive) {
          return
        }

        startTransition(() => {
          setHttpState({
            status,
            players,
            error: null,
          })
        })
      } catch (error) {
        if (!isActive || abortController.signal.aborted) {
          return
        }

        startTransition(() => {
          setHttpState((currentState) => ({
            ...currentState,
            error:
              error instanceof Error
                ? error.message
                : 'Falha ao buscar bootstrap HTTP do backend.',
          }))
        })
      }
    }

    void bootstrapFromHttp()

    return () => {
      isActive = false
      abortController.abort()
    }
  }, [])

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="hero-copy">
          <p className="eyebrow">Futebol Cooperativo SMA</p>
          <h1>Simulação da partida.</h1>
          <p className="lede">
            Exibição do campo, dos jogadores e da bola durante a execução do jogo.
          </p>
        </div>

        <span className={`connection-chip is-${connectionStatus}`}>
          {getConnectionLabel(connectionStatus)}
        </span>
      </header>

      <main className="dashboard">
        <section className="panel viewport-panel">
          <div className="panel-heading">
            <div>
              <span className="panel-kicker">Visualização</span>
              <h2>Campo da partida</h2>
            </div>
          </div>

          <div className="match-score">
            <span className="match-score-team match-score-azul">Azul</span>
            <strong className="match-score-value">
              {gameState.scoreboard.A} &times; {gameState.scoreboard.B}
            </strong>
            <span className="match-score-team match-score-vermelho">Vermelho</span>
          </div>

          <GameViewport state={gameState} />
          <MatchStatePanel state={gameState} />
        </section>

        <div className="panels-row">
          <Scoreboard
            connectionStatus={connectionStatus}
            tempo={gameState.tempo}
            playerCount={gameState.players.length}
            ball={gameState.ball}
            ballCarrierId={ballCarrier?.id ?? null}
          />
          <DebugPanel
            connectionStatus={connectionStatus}
            gameState={gameState}
            lastRawMessage={lastRawMessage}
            events={events}
            httpStatus={httpState.status}
            httpPlayersCount={httpState.players ? Object.keys(httpState.players).length : null}
            httpError={httpState.error}
            wsUrl={env.wsUrl}
            apiUrl={env.apiUrl}
          />
        </div>
      </main>
    </div>
  )
}

function getConnectionLabel(connectionStatus: string): string {
  switch (connectionStatus) {
    case 'connecting':
      return 'Conectando'
    case 'connected':
      return 'Ao vivo'
    case 'reconnecting':
      return 'Reconectando'
    case 'disconnected':
      return 'Desconectado'
    case 'error':
      return 'Erro'
    default:
      return 'Parado'
  }
}

export default App
