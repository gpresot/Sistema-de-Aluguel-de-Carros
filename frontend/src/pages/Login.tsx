import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api'
import { useAuth } from '../auth/context'
import AuthLayout from '../components/AuthLayout'
import PasswordField from '../components/PasswordField'
import TextField from '../components/TextField'

interface LocationState {
  from?: string
  mensagem?: string
}

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const state = (useLocation().state ?? {}) as LocationState
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await login(email, senha)
      navigate(state.from ?? '/', { replace: true })
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Erro inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <AuthLayout titulo="Entrar" subtitulo="Acesse o sistema de aluguel de carros">
      {state.mensagem && <p className="alerta sucesso">{state.mensagem}</p>}
      {erro && <p className="alerta erro" role="alert">{erro}</p>}
      <form onSubmit={enviar}>
        <TextField id="email" label="E-mail" type="email" autoComplete="username" value={email} onChange={setEmail} />
        <PasswordField id="senha" label="Senha" autoComplete="current-password" value={senha} onChange={setSenha} />
        <button className="btn" type="submit" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
      <p className="links">
        <Link to="/recuperar-senha">Esqueci minha senha</Link>
        <Link to="/cadastro">Criar conta</Link>
      </p>
    </AuthLayout>
  )
}
