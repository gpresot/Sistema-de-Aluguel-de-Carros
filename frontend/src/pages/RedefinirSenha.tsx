import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError, request } from '../api'
import AuthLayout from '../components/AuthLayout'
import PasswordField from '../components/PasswordField'
import type { Mensagem } from '../types'

type Estado = 'verificando' | 'valido' | 'invalido'

export default function RedefinirSenha() {
  const token = useSearchParams()[0].get('token') ?? ''
  const navigate = useNavigate()
  const [estado, setEstado] = useState<Estado>('verificando')
  const [senha, setSenha] = useState('')
  const [confirmacaoSenha, setConfirmacaoSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    let cancelado = false
    request<void>(`/api/auth/redefinir-senha/validar?token=${encodeURIComponent(token)}`)
      .then(() => !cancelado && setEstado('valido'))
      .catch(() => !cancelado && setEstado('invalido'))
    return () => {
      cancelado = true
    }
  }, [token])

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro('')
    if (senha !== confirmacaoSenha) {
      setErro('As senhas não coincidem.')
      return
    }
    setEnviando(true)
    try {
      const r = await request<Mensagem>('/api/auth/redefinir-senha', {
        method: 'POST',
        body: { token, senha, confirmacaoSenha },
      })
      navigate('/login', { replace: true, state: { mensagem: r.mensagem } })
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Erro inesperado.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <AuthLayout titulo="Nova senha">
      {estado === 'verificando' && <p>Verificando link...</p>}
      {estado === 'invalido' && (
        <>
          <p className="alerta erro">Link inválido ou expirado. Solicite outro.</p>
          <p className="links">
            <Link to="/recuperar-senha">Solicitar novo link</Link>
          </p>
        </>
      )}
      {estado === 'valido' && (
        <>
          {erro && <p className="alerta erro" role="alert">{erro}</p>}
          <form onSubmit={enviar}>
            <PasswordField id="senha" label="Nova senha" autoComplete="new-password" value={senha} onChange={setSenha} />
            <PasswordField id="confirmacaoSenha" label="Confirmar nova senha" autoComplete="new-password" value={confirmacaoSenha} onChange={setConfirmacaoSenha} />
            <button className="btn" type="submit" disabled={enviando}>
              {enviando ? 'Salvando...' : 'Redefinir senha'}
            </button>
          </form>
        </>
      )}
    </AuthLayout>
  )
}
