import { useEffect, useState } from 'react'
import { request } from '../api'
import { useAuth } from '../auth/context'
import { Link } from 'react-router-dom'
import Topo from '../components/Topo'

interface Me {
  email: string
  perfil: string
}

const DESCRICAO: Record<string, string> = {
  CLIENTE: 'Escolha um carro no catálogo, peça o aluguel e acompanhe o status do seu pedido.',
  EMPRESA: 'Aceite ou recuse os pedidos dos seus carros e gerencie a frota (com fotos e valor da diária).',
  BANCO: 'Analise a renda dos clientes e aprove ou reprove os pedidos aceitos pelas empresas.',
}

export default function Home() {
  const { sessao } = useAuth()
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

  const ehCliente = sessao.perfil === 'CLIENTE'

  return (
    <main className="home">
      <Topo />
      <section className="card card-largo">
        <h1>Olá, {sessao.nome}</h1>
        <p>{DESCRICAO[sessao.perfil]}</p>
        <p className="subtitulo">
          Sessão validada no servidor: {me ? `${me.email} (${me.perfil})` : 'verificando...'}
        </p>
        <div className="acoes-home">
          {ehCliente && <Link className="btn-link" to="/catalogo">Ver catálogo</Link>}{' '}
          <Link className="btn-link" to="/pedidos">{ehCliente ? 'Meus pedidos' : 'Pedidos'}</Link>{' '}
          <Link className="btn-link" to="/automoveis">{ehCliente ? 'Meus automóveis' : 'Gerenciar frota'}</Link>
        </div>
      </section>
    </main>
  )
}