const BASE: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

/** Monta a URL absoluta de um caminho da API (ex.: fotos servidas pelo back-end). */
export const urlApi = (caminho: string) => `${BASE}${caminho}`

export class ApiError extends Error {
  status: number
  campos?: Record<string, string>

  constructor(status: number, message: string, campos?: Record<string, string>) {
    super(message)
    this.status = status
    this.campos = campos
  }
}

let token: string | null = null
let onUnauthorized: (() => void) | null = null

export function setToken(novo: string | null) {
  token = novo
}

export function setUnauthorizedHandler(fn: (() => void) | null) {
  onUnauthorized = fn
}

interface Options {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  /** Envio de arquivos (multipart); o navegador define o Content-Type com o boundary. */
  form?: FormData
}

export async function request<T>(path: string, options: Options = {}): Promise<T> {
  const headers: Record<string, string> = {}
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'
  if (token) headers.Authorization = `Bearer ${token}`

  let res: Response
  try {
    res = await fetch(`${BASE}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.form ?? (options.body !== undefined ? JSON.stringify(options.body) : undefined),
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.')
  }

  if (res.status === 204) return undefined as T

  const data = await res.json().catch(() => null)
  if (!res.ok) {
    if (res.status === 401 && token) {
      onUnauthorized?.()
      throw new ApiError(401, 'Sessão expirada. Entre novamente.')
    }
    throw new ApiError(res.status, data?.erro ?? 'Erro inesperado.', data?.campos)
  }
  return data as T
}
