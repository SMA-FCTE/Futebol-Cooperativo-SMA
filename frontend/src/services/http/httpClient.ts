import { env } from '../../config/env'

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }

  const response = await fetch(new URL(path, env.apiUrl), {
    ...init,
    headers,
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status} ao acessar ${path}`)
  }

  return (await response.json()) as T
}
