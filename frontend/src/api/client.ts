const DEV_PROXY_BASE = '/api'
const rawBaseUrl = import.meta.env.DEV
  ? DEV_PROXY_BASE
  : import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:2800'

const baseUrl = rawBaseUrl.replace(/\/$/, '')

let authHeader: string | undefined

export function setBasicAuth(username: string, password: string) {
  if (username && password) {
    authHeader = `Basic ${btoa(`${username}:${password}`)}`
  } else {
    authHeader = undefined
  }
}

export function clearAuth() {
  authHeader = undefined
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(authHeader ? { Authorization: authHeader } : {}),
      ...init.headers,
    },
    ...init,
  })

  if (!response.ok) {
    const message = await safeReadError(response)
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

async function safeReadError(response: Response): Promise<string> {
  try {
    const data = await response.json()
    if (typeof data?.message === 'string') {
      return data.message
    }
  } catch {
    // ignore JSON parse errors and fall back to status text
  }
  return response.statusText || 'Unknown error'
}
