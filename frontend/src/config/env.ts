function resolveUrl(value: string | undefined, fallback: string): string {
  const trimmed = value?.trim()
  return trimmed && trimmed.length > 0 ? trimmed : fallback
}

export const env = {
  apiUrl: resolveUrl(import.meta.env.VITE_GAME_API_URL, 'http://localhost:8080'),
  wsUrl: resolveUrl(import.meta.env.VITE_GAME_WS_URL, 'ws://localhost:9090'),
} as const
