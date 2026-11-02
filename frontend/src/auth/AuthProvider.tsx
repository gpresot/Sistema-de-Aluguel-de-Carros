import { useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { request, setToken, setUnauthorizedHandler } from '../api'
import type { AuthResponse, RegistroClienteInput } from '../types'
import { AuthContext } from './context'
import type { AuthContextValue, Sessao } from './context'

const STORAGE_KEY = 'aluguel.sessao'

function carregar(): Sessao | null {
  try {
    const bruto = localStorage.getItem(STORAGE_KEY)
    return bruto ? (JSON.parse(bruto) as Sessao) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => {
    const salva = carregar()
    // O token precisa estar disponível antes das requisições dos componentes filhos.
    setToken(salva?.token ?? null)
    return salva
  })

  const guardar = useCallback((nova: Sessao | null) => {
    setToken(nova?.token ?? null)
    setSessao(nova)
    try {
      if (nova) localStorage.setItem(STORAGE_KEY, JSON.stringify(nova))
      else localStorage.removeItem(STORAGE_KEY)
    } catch {
      // armazenamento indisponível: a sessão vale só até recarregar a página
    }
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(() => guardar(null))
    return () => setUnauthorizedHandler(null)
  }, [guardar])

  const value = useMemo<AuthContextValue>(
    () => ({
      sessao,
      async login(email, senha) {
        guardar(await request<AuthResponse>('/api/auth/login', { method: 'POST', body: { email, senha } }))
      },
      async registrar(dados: RegistroClienteInput) {
        guardar(await request<AuthResponse>('/api/auth/registro', { method: 'POST', body: dados }))
      },
      logout() {
        guardar(null)
      },
    }),
    [sessao, guardar],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
