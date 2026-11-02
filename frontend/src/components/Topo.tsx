import { Link } from 'react-router-dom'
import { useAuth } from '../auth/context'

export default function Topo() {
  const { sessao, logout } = useAuth()
  if (!sessao) return null
  const cliente = sessao.perfil === 'CLIENTE'
  return (
    <header className="topo">
      <nav className="nav">
        <strong>Aluguel de Carros</strong>
        <Link to="/">Início</Link>
        {cliente && <Link to="/catalogo">Catálogo</Link>}
        <Link to="/pedidos">Pedidos</Link>
        <Link to="/automoveis">{cliente ? 'Meus automóveis' : 'Frota'}</Link>
      </nav>
      <div>
        <span className="usuario">{sessao.nome} · {sessao.perfil}</span>
        <button className="btn-secundario" onClick={logout}>Sair</button>
      </div>
    </header>
  )
}
