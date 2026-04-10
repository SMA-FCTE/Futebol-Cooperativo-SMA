import { Graphics } from 'pixi.js'

import { mapFieldPoint, type FieldViewport } from '../mapping/coordinateMapper'
import type { BallState } from '../model/gameTypes'

export class BallLayer {
  readonly displayObject = new Graphics()

  render(ball: BallState | null, viewport: FieldViewport): void {
    const graphics = this.displayObject

    graphics.clear()

    if (!ball) {
      graphics.visible = false
      return
    }

    const radius = Math.max(4, Math.min(9, viewport.scale * 0.42))
    const position = mapFieldPoint(ball.x, ball.y, viewport)

    graphics.visible = true
    graphics.circle(position.x, position.y, radius)
    graphics.fill({ color: 0xf8fafc })
    graphics.circle(position.x, position.y, radius)
    graphics.stroke({ color: 0x0f172a, width: Math.max(1.5, radius * 0.35) })

    if (ball.emPosseDe) {
      graphics.circle(position.x, position.y, radius + 3)
      graphics.stroke({ color: 0xfacc15, width: 1.5 })
    }
  }
}
