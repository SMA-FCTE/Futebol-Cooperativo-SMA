import { Container, Graphics, Text } from 'pixi.js'

import { mapFieldPoint, type FieldViewport } from '../mapping/coordinateMapper'
import type { PlayerState, TeamId } from '../model/gameTypes'

type PlayerNode = {
  container: Container
  body: Graphics
  label: Text
}

export class PlayerLayer {
  readonly container = new Container()

  private readonly nodes = new Map<string, PlayerNode>()

  render(players: PlayerState[], viewport: FieldViewport): void {
    const activeIds = new Set<string>()

    for (const player of players) {
      activeIds.add(player.id)

      let node = this.nodes.get(player.id)
      if (!node) {
        node = this.createNode(player.id)
        this.nodes.set(player.id, node)
        this.container.addChild(node.container)
      }

      this.updateNode(node, player, viewport)
    }

    for (const [id, node] of this.nodes) {
      if (activeIds.has(id)) {
        continue
      }

      this.container.removeChild(node.container)
      node.container.destroy({ children: true })
      this.nodes.delete(id)
    }
  }

  private createNode(id: string): PlayerNode {
    const container = new Container()
    const body = new Graphics()
    const label = new Text({
      text: id,
      style: {
        fill: 0xf6fffb,
        fontFamily: 'Trebuchet MS',
        fontSize: 12,
        fontWeight: '700',
      },
    })

    container.eventMode = 'none'
    label.anchor.set(0.5, 0)
    label.resolution = 2
    container.addChild(body)
    container.addChild(label)

    return { container, body, label }
  }

  private updateNode(node: PlayerNode, player: PlayerState, viewport: FieldViewport): void {
    const radius = Math.max(8, Math.min(16, viewport.scale * 0.78))
    const position = mapFieldPoint(player.x, player.y, viewport)

    node.container.position.set(position.x, position.y)
    node.label.text = player.id
    node.label.position.set(0, radius + 4)
    node.label.visible = viewport.scale >= 5.25

    node.body.clear()
    node.body.circle(0, 0, radius)
    node.body.fill({ color: this.getTeamColor(player.team) })
    node.body.circle(0, 0, radius)
    node.body.stroke({ color: 0x103123, width: Math.max(2, radius * 0.22) })
  }

  private getTeamColor(team: TeamId): number {
    switch (team) {
      case 'A':
        return 0x1d4ed8
      case 'B':
        return 0xdc2626
      default:
        return 0xf59e0b
    }
  }
}
