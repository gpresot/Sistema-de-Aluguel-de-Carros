import { useEffect, useState } from 'react'
import { request } from '../api'
import { useAuth } from '../auth/context'
import { Link } from 'react-router-dom'

interface Me {
  email: string
  perfil: string
}

const DESCRICAO: Record<string, string> = {
  CLIENTE: 'Aqui você vai criar, modificar, consultar e cancelar seus pedidos de aluguel.',
  EMPRESA: 'Aqui você vai avaliar pedidos, executar contratos e gerenciar automóveis.',
  BANCO: 'Aqui você vai avaliar pedidos, conceder crédito, executar contratos e gerenciar automóveis.',
}

export default function Home() {
  const { sessao, logout } = useAuth()
  const [me, setMe] = useState<Me | null>(null)

  useEffect(() => {
    let cancelado = false
    request<Me>('/api/auth/me')
      .then((m) => !cancelado && setMe(m))
      .catch(() => undefined)
    return () => {
      cancelado = true
    }
  }, [])

  if (!sessao) return null

  return (
    <main className="home">
      <header className="topo">
        <strong>Aluguel de Carros</strong>
        <div>
          <span className="usuario">{sessao.nome} · {sessao.perfil}</span>
          <button className="btn-secundario" onClick={logout}>Sair</button>
        </div>
      </header>
      <section className="card card-largo">
        <h1>Olá, {sessao.nome}</h1>
        <p>{DESCRICAO[sessao.perfil]}</p>
        <p className="subtitulo">
          Sessão validada no servidor: {me ? `${me.email} (${me.perfil})` : 'verificando...'}
        </p>
        <div className="acoes-home"><Link className="btn-link" to="/automoveis">Gerenciar automóveis</Link><p className="alerta">Os pedidos de aluguel entram no próximo passo.</p></div>
      </section>
    </main>
  )
}
