import type { FieldState } from '../model/gameTypes'

export type FieldViewport = {
  field: FieldState
  x: number
  y: number
  width: number
  height: number
  scale: number
  padding: number
}

export function createFieldViewport(
  viewportWidth: number,
  viewportHeight: number,
  field: FieldState,
  padding = 24,
): FieldViewport {
  const safeWidth = Math.max(viewportWidth, 320)
  const safeHeight = Math.max(viewportHeight, 240)
  const innerWidth = Math.max(safeWidth - padding * 2, 1)
  const innerHeight = Math.max(safeHeight - padding * 2, 1)
  const scale = Math.min(innerWidth / field.width, innerHeight / field.height)
  const width = field.width * scale
  const height = field.height * scale

  return {
    field,
    x: (safeWidth - width) / 2,
    y: (safeHeight - height) / 2,
    width,
    height,
    scale,
    padding,
  }
}

export function mapFieldPoint(x: number, y: number, viewport: FieldViewport) {
  return {
    x: viewport.x + clamp(x, 0, viewport.field.width) * viewport.scale,
    y: viewport.y + clamp(y, 0, viewport.field.height) * viewport.scale,
  }
}

export function mapFieldSize(units: number, viewport: FieldViewport): number {
  return Math.max(units * viewport.scale, 0)
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}
