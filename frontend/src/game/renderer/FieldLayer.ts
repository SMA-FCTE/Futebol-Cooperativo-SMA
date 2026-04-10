import { Graphics } from 'pixi.js'

import { mapFieldSize, type FieldViewport } from '../mapping/coordinateMapper'

export class FieldLayer {
  readonly displayObject = new Graphics()

  private lastSignature = ''

  render(viewport: FieldViewport): void {
    const signature = `${viewport.width.toFixed(2)}:${viewport.height.toFixed(2)}`

    if (signature === this.lastSignature) {
      return
    }

    this.lastSignature = signature

    const graphics = this.displayObject
    const borderColor = 0xf4fff6
    const pitchColor = 0x2b8a3e
    const lineWidth = Math.max(2, viewport.scale * 0.18)
    const centerX = viewport.x + viewport.width / 2
    const centerY = viewport.y + viewport.height / 2
    const centerCircleRadius = mapFieldSize(9.15, viewport)
    const penaltyDepth = viewport.width * 0.16
    const penaltyHeight = viewport.height * 0.36
    const goalAreaDepth = penaltyDepth * 0.44
    const goalAreaHeight = penaltyHeight * 0.56
    const cornerRadius = Math.max(10, viewport.scale * 1.25)

    graphics.clear()
    graphics.roundRect(viewport.x, viewport.y, viewport.width, viewport.height, cornerRadius)
    graphics.fill({ color: pitchColor })
    graphics.roundRect(viewport.x, viewport.y, viewport.width, viewport.height, cornerRadius)
    graphics.stroke({ color: borderColor, width: lineWidth })

    graphics.moveTo(centerX, viewport.y)
    graphics.lineTo(centerX, viewport.y + viewport.height)
    graphics.stroke({ color: borderColor, width: lineWidth })

    graphics.circle(centerX, centerY, centerCircleRadius)
    graphics.stroke({ color: borderColor, width: lineWidth })
    graphics.circle(centerX, centerY, Math.max(3, lineWidth))
    graphics.fill({ color: borderColor })

    this.drawArea(
      graphics,
      viewport.x,
      centerY - penaltyHeight / 2,
      penaltyDepth,
      penaltyHeight,
      lineWidth,
      borderColor,
    )
    this.drawArea(
      graphics,
      viewport.x + viewport.width - penaltyDepth,
      centerY - penaltyHeight / 2,
      penaltyDepth,
      penaltyHeight,
      lineWidth,
      borderColor,
    )
    this.drawArea(
      graphics,
      viewport.x,
      centerY - goalAreaHeight / 2,
      goalAreaDepth,
      goalAreaHeight,
      lineWidth,
      borderColor,
    )
    this.drawArea(
      graphics,
      viewport.x + viewport.width - goalAreaDepth,
      centerY - goalAreaHeight / 2,
      goalAreaDepth,
      goalAreaHeight,
      lineWidth,
      borderColor,
    )
  }

  private drawArea(
    graphics: Graphics,
    x: number,
    y: number,
    width: number,
    height: number,
    lineWidth: number,
    color: number,
  ): void {
    graphics.rect(x, y, width, height)
    graphics.stroke({ color, width: lineWidth })
  }
}
