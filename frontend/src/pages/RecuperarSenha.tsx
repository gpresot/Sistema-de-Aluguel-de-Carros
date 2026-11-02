import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, request } from '../api'
import AuthLayout from '../components/AuthLayout'
import TextField from '../components/TextField'
import type { Mensagem } from '../types'

export default function RecuperarSenha() {
  const [email, setEmail] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro('')
    setSucesso('')
    setEnviando(true)
    try {
      const r = await request<Mensagem>('/api/auth/recuperar-senha', { method: 'POST', body: { email } })
      setSucesso(r.mensagem)
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Erro inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <AuthLayout titulo="Recuperar senha" subtitulo="Enviaremos um link para você definir uma nova senha">
      {sucesso && <p className="alerta sucesso">{sucesso}</p>}
      {erro && <p className="alerta erro" role="alert">{erro}</p>}
      <form onSubmit={enviar}>
        <TextField id="email" label="E-mail" type="email" autoComplete="email" value={email} onChange={setEmail} />
        <button className="btn" type="submit" disabled={enviando}>
          {enviando ? 'Enviando...' : 'Enviar link'}
        </button>
      </form>
      <p className="links">
        <Link to="/login">Voltar ao login</Link>
      </p>
    </AuthLayout>
  )
}
