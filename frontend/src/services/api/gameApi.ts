import { request } from '../http/httpClient'

export type BackendPlayerState = {
  x: number
  y: number
  velocidade: number
  theta?: number
  comBola?: boolean
}

export type BackendPlayersResponse = Record<string, BackendPlayerState>

export const gameApi = {
  getPlayers(init?: RequestInit) {
    return request<BackendPlayersResponse>('/api/jogadores', init)
  },
  getStatus(init?: RequestInit) {
    return request<string>('/api/status', init)
  },
  iniciarPartida(duracaoSegundos: number, init?: RequestInit) {
    const headers = new Headers(init?.headers)
    headers.set('Content-Type', 'application/json')

    return request<{ ok: boolean }>('/api/partida/iniciar', {
      ...init,
      method: 'POST',
      headers,
      body: JSON.stringify({ duracaoSegundos }),
    })
  },
  reiniciarPartida(init?: RequestInit) {
    return request<{ ok: boolean }>('/api/partida/reiniciar', {
      ...init,
      method: 'POST',
    })
  },
}
