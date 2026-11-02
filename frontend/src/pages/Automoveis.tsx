import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, request, urlApi } from '../api'
import Topo from '../components/Topo'
import { moeda } from '../format'
import type { Automovel } from '../types/automovel'

type Dados = { matricula: string; ano: number; marca: string; modelo: string; placa: string; valorDiaria: string }
const vazio: Dados = { matricula: '', ano: new Date().getFullYear(), marca: '', modelo: '', placa: '', valorDiaria: '' }

export default function Automoveis() {
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
      await request<Automovel>(editando ? `/api/automoveis/${editando}` : '/api/automoveis', { method: editando ? 'PUT' : 'POST', body: { ...dados, valorDiaria: Number(dados.valorDiaria.replace(',', '.')) } })
      setSucesso(editando ? 'Automóvel atualizado.' : 'Automóvel cadastrado.')
      setDados(vazio); setEditando(null); await carregar()
    } catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível salvar o automóvel.') }
    finally { setSalvando(false) }
  }

  function editar(item: Automovel) {
    setEditando(item.id)
    setDados({ matricula: item.matricula, ano: item.ano, marca: item.marca, modelo: item.modelo, placa: item.placa, valorDiaria: String(item.valorDiaria) })
    setErro(''); setSucesso('')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function enviarFoto(id: number, arquivo: File | undefined) {
    if (!arquivo) return
    setErro(''); setSucesso('')
    const form = new FormData()
    form.append('arquivo', arquivo)
    try { await request<Automovel>(`/api/automoveis/${id}/foto`, { method: 'POST', form }); setSucesso('Foto atualizada.'); await carregar() }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível enviar a foto.') }
  }

  async function removerFoto(id: number) {
    setErro(''); setSucesso('')
    try { await request<Automovel>(`/api/automoveis/${id}/foto`, { method: 'DELETE' }); setSucesso('Foto removida.'); await carregar() }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível remover a foto.') }
  }

  async function excluir(id: number) {
    if (!window.confirm('Deseja realmente excluir este automóvel?')) return
    setErro(''); setSucesso('')
    try { await request<void>(`/api/automoveis/${id}`, { method: 'DELETE' }); setSucesso('Automóvel excluído.'); await carregar() }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível excluir o automóvel.') }
  }

  return (
    <main className="home">
      <Topo />

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
              <label className="campo"><span>Valor da diária (R$)</span><input required type="number" min="0.01" step="0.01" value={dados.valorDiaria} onChange={e => alterar('valorDiaria', e.target.value)} placeholder="150.00" /></label>
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
                  <div className="car-card-imagem">
                    {item.fotoUrl ? <img src={urlApi(item.fotoUrl)} alt={`${item.marca} ${item.modelo}`} loading="lazy" /> : <span aria-hidden="true">🚗</span>}
                  </div>
                  <div className="car-card-corpo">
                    <p className="eyebrow">{item.ano}</p>
                    <h3>{item.marca} {item.modelo}</h3>
                    <p className="car-card-placa">Placa {item.placa} · Matrícula {item.matricula}</p>
                    <p className="car-card-preco">{item.valorDiaria > 0 ? <>{moeda(item.valorDiaria)} <small>/ dia</small></> : <small>Sem valor de diária: edite para definir</small>}</p>
                    <div className="acoes-tabela">
                      <label className="btn-secundario btn-arquivo">
                        {item.fotoUrl ? 'Trocar foto' : 'Adicionar foto'}
                        <input type="file" accept="image/jpeg,image/png,image/webp" hidden
                          onChange={e => { void enviarFoto(item.id, e.target.files?.[0]); e.target.value = '' }} />
                      </label>
                      {item.fotoUrl && <button className="btn-secundario" onClick={() => void removerFoto(item.id)}>Remover foto</button>}
                    </div>
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