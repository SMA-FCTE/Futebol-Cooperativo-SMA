import { useEffect, useRef } from 'react'

import type { GameState } from '../../game/model/gameTypes'
import { PixiGameApp } from '../../game/renderer/PixiGameApp'

type GameViewportProps = {
  state: GameState
}

function GameViewport({ state }: GameViewportProps) {
  const hostRef = useRef<HTMLDivElement | null>(null)
  const pixiAppRef = useRef<PixiGameApp | null>(null)
  const latestStateRef = useRef(state)

  useEffect(() => {
    const host = hostRef.current
    if (!host) {
      return
    }

    const pixiApp = new PixiGameApp()
    let destroyed = false

    pixiAppRef.current = pixiApp

    void pixiApp.mount(host).then(() => {
      if (destroyed) {
        return
      }

      pixiApp.render(latestStateRef.current)
    })

    return () => {
      destroyed = true
      pixiAppRef.current = null
      pixiApp.destroy()
    }
  }, [])

  useEffect(() => {
    latestStateRef.current = state
    pixiAppRef.current?.render(state)
  }, [state])

  return <div ref={hostRef} className="game-viewport" aria-label="Campo renderizado pelo PixiJS" />
}

export default GameViewport
