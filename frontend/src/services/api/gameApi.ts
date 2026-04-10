import { request } from '../http/httpClient'

export type BackendPlayerState = {
  x: number
  y: number
  velocidade: number
  theta: number
}

export type BackendPlayersResponse = Record<string, BackendPlayerState>

export const gameApi = {
  getPlayers(init?: RequestInit) {
    return request<BackendPlayersResponse>('/api/jogadores', init)
  },
  getStatus(init?: RequestInit) {
    return request<string>('/api/status', init)
  },
}
