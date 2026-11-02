import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, request } from '../api'
import { useAuth } from '../auth/context'

type Automovel = { id: number; matricula: string; ano: number; marca: string; modelo: string; placa: string; tipoProprietario: string }
type Dados = { matricula: string; ano: number; marca: string; modelo: string; placa: string }
const vazio: Dados = { matricula: '', ano: new Date().getFullYear(), marca: '', modelo: '', placa: '' }

export default function Automoveis() {
  const { sessao } = useAuth()
  const [lista, setLista] = useState<Automovel[]>([])
  const [dados, setDados] = useState<Dados>(vazio)
  const [editando, setEditando] = useState<number | null>(null)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try { setLista(await request<Automovel[]>('/api/automoveis')); setErro('') }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar os automóveis.') }
    finally { setCarregando(false) }
  }, [])

  useEffect(() => { void carregar() }, [carregar])

  function alterar(campo: keyof Dados, valor: string) {
    setDados(atual => ({ ...atual, [campo]: campo === 'ano' ? Number(valor) : valor }))
  }

  async function salvar(event: FormEvent) {
    event.preventDefault(); setErro(''); setSucesso(''); setSalvando(true)
    try {
      await request<Automovel>(editando ? `/api/automoveis/${editando}` : '/api/automoveis', { method: editando ? 'PUT' : 'POST', body: dados })
      setSucesso(editando ? 'Automóvel atualizado.' : 'Automóvel cadastrado.')
      setDados(vazio); setEditando(null); await carregar()
    } catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível salvar o automóvel.') }
    finally { setSalvando(false) }
  }

  function editar(item: Automovel) {
    setEditando(item.id)
    setDados({ matricula: item.matricula, ano: item.ano, marca: item.marca, modelo: item.modelo, placa: item.placa })
    setErro(''); setSucesso('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function excluir(id: number) {
    if (!window.confirm('Deseja realmente excluir este automóvel?')) return
    setErro(''); setSucesso('')
    try { await request<void>(`/api/automoveis/${id}`, { method: 'DELETE' }); setSucesso('Automóvel excluído.'); await carregar() }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível excluir o automóvel.') }
  }

  return (
    <main className="home">
      <header className="topo">
        <strong>Aluguel de Carros</strong>
        <div>
          <span className="usuario">{sessao?.nome} · {sessao?.perfil}</span>{' '}
          <Link to="/" className="btn-secundario">Início</Link>
        </div>
      </header>

      <div className="pagina-automoveis">
        <div className="cabecalho-pagina">
          <div>
            <p className="eyebrow">GESTÃO DE FROTA</p>
            <h1>Automóveis</h1>
            <p className="subtitulo">Cadastre e gerencie os automóveis vinculados à sua conta.</p>
          </div>
          <span className="contador">{lista.length} {lista.length === 1 ? 'veículo' : 'veículos'}</span>
        </div>

        <section className="painel-automoveis">
          <h2>{editando ? 'Editar automóvel' : 'Cadastrar automóvel'}</h2>
          {erro && <p className="alerta erro">{erro}</p>}
          {sucesso && <p className="alerta sucesso">{sucesso}</p>}
          <form onSubmit={salvar}>
            <div className="grade-form">
              <label className="campo"><span>Matrícula</span><input required value={dados.matricula} onChange={e => alterar('matricula', e.target.value)} placeholder="Identificação interna" /></label>
              <label className="campo"><span>Ano</span><input required type="number" min="1886" max={new Date().getFullYear() + 1} value={dados.ano} onChange={e => alterar('ano', e.target.value)} /></label>
              <label className="campo"><span>Marca</span><input required value={dados.marca} onChange={e => alterar('marca', e.target.value)} placeholder="Ex.: Toyota" /></label>
              <label className="campo"><span>Modelo</span><input required value={dados.modelo} onChange={e => alterar('modelo', e.target.value)} placeholder="Ex.: Corolla" /></label>
              <label className="campo"><span>Placa</span><input required maxLength={7} value={dados.placa} onChange={e => alterar('placa', e.target.value.toUpperCase())} placeholder="ABC1D23" /></label>
            </div>
            <div className="botoes-form">
              <button className="btn" disabled={salvando}>{salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Cadastrar automóvel'}</button>
              {editando && <button type="button" className="btn-secundario" onClick={() => { setEditando(null); setDados(vazio) }}>Cancelar edição</button>}
            </div>
          </form>
        </section>

        <section className="lista-automoveis">
          <div className="titulo-lista">
            <h2>Minha frota</h2>
            <button className="btn-secundario" onClick={() => void carregar()}>Atualizar lista</button>
          </div>
          {carregando ? (
            <p className="estado-vazio">Carregando automóveis...</p>
          ) : lista.length === 0 ? (
            <p className="estado-vazio">Nenhum automóvel cadastrado ainda.</p>
          ) : (
            <div className="car-grid">
              {lista.map(item => (
                <article key={item.id} className="car-card">
                  <div className="car-card-imagem" aria-hidden="true">🚗</div>
                  <div className="car-card-corpo">
                    <p className="eyebrow">{item.ano}</p>
                    <h3>{item.marca} {item.modelo}</h3>
                    <p className="car-card-placa">Placa {item.placa} · Matrícula {item.matricula}</p>
                    <div className="acoes-tabela">
                      <button className="btn-secundario" onClick={() => editar(item)}>Editar</button>
                      <button className="btn-perigo" onClick={() => void excluir(item.id)}>Excluir</button>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  )
}