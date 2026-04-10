import { Application, Container } from 'pixi.js'

import { createFieldViewport } from '../mapping/coordinateMapper'
import { createEmptyGameState } from '../model/gameState'
import type { GameState } from '../model/gameTypes'
import { BallLayer } from './BallLayer'
import { FieldLayer } from './FieldLayer'
import { PlayerLayer } from './PlayerLayer'

export class PixiGameApp {
  private app: Application | null = null

  private host: HTMLElement | null = null

  private readonly scene = new Container()

  private readonly fieldLayer = new FieldLayer()

  private readonly playerLayer = new PlayerLayer()

  private readonly ballLayer = new BallLayer()

  private resizeObserver: ResizeObserver | null = null

  private state: GameState = createEmptyGameState()

  private lastSize = { width: 0, height: 0 }

  async mount(host: HTMLElement): Promise<void> {
    if (this.app) {
      return
    }

    this.host = host

    const app = new Application()
    await app.init({
      antialias: true,
      autoDensity: true,
      backgroundAlpha: 0,
      resolution: window.devicePixelRatio || 1,
    })

    this.app = app
    this.scene.eventMode = 'none'
    this.scene.addChild(this.fieldLayer.displayObject)
    this.scene.addChild(this.ballLayer.displayObject)
    this.scene.addChild(this.playerLayer.container)
    app.stage.addChild(this.scene)
    app.canvas.classList.add('game-canvas')
    host.appendChild(app.canvas)

    this.resize()
    this.resizeObserver = new ResizeObserver(() => this.resize())
    this.resizeObserver.observe(host)
  }

  render(state: GameState): void {
    this.state = state
    this.redraw()
  }

  destroy(): void {
    this.resizeObserver?.disconnect()
    this.resizeObserver = null

    if (this.app && this.host && this.app.canvas.parentElement === this.host) {
      this.host.removeChild(this.app.canvas)
    }

    this.app?.destroy()
    this.app = null
    this.host = null
    this.lastSize = { width: 0, height: 0 }
  }

  private resize(): void {
    if (!this.app || !this.host) {
      return
    }

    const width = Math.max(this.host.clientWidth, 320)
    const height = Math.max(this.host.clientHeight, 240)

    if (width === this.lastSize.width && height === this.lastSize.height) {
      return
    }

    this.lastSize = { width, height }
    this.app.renderer.resize(width, height)
    this.redraw()
  }

  private redraw(): void {
    if (!this.app) {
      return
    }

    const width = this.lastSize.width || this.app.renderer.width
    const height = this.lastSize.height || this.app.renderer.height
    const viewport = createFieldViewport(width, height, this.state.field, 24)

    this.fieldLayer.render(viewport)
    this.playerLayer.render(this.state.players, viewport)
    this.ballLayer.render(this.state.ball, viewport)
  }
}
