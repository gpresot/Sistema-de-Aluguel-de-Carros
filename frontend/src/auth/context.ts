import { createContext, useContext } from 'react'
import type { Perfil, RegistroClienteInput } from '../types'

export interface Sessao {
  token: string
  email: string
  perfil: Perfil
  nome: string
}

export interface AuthContextValue {
  sessao: Sessao | null
  login: (email: string, senha: string) => Promise<void>
  registrar: (dados: RegistroClienteInput) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return ctx
}
