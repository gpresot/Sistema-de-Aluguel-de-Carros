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
  EMPRESA: 'Aceite ou recuse os pedidos dos seus carros, execute contratos de pedidos aprovados e gerencie a frota (com fotos e valor da diária).',
  BANCO: 'Analise a renda dos clientes, aprove ou reprove pedidos, conceda crédito e execute contratos.',
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
      <div className="pagina-automoveis pagina-estreita">
        <h1>Olá, {sessao.nome}</h1>
        <p>{DESCRICAO[sessao.perfil]}</p>
        {me && <p className="subtitulo">Conectado como {me.email}.</p>}
        <div className="acoes-home">
          {ehCliente && <Link className="btn-link" to="/catalogo">Ver catálogo</Link>}
          <Link className={ehCliente ? 'btn-link secundario' : 'btn-link'} to="/pedidos">{ehCliente ? 'Meus pedidos' : 'Pedidos'}</Link>
          <Link className="btn-link secundario" to="/automoveis">{ehCliente ? 'Meus automóveis' : 'Gerenciar frota'}</Link>
          {ehCliente && <Link className="btn-link secundario" to="/meu-cadastro">Meu cadastro</Link>}
        </div>
      </div>
    </main>
  )
}