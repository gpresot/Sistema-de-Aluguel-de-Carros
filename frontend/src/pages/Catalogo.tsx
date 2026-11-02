import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, request } from '../api'
import CarList from '../components/CarList'
import Topo from '../components/Topo'
import { hojeIso, moeda } from '../format'
import type { Automovel } from '../types/automovel'

export default function Catalogo() {
  const [carros, setCarros] = useState<Automovel[]>([])
  const [busca, setBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [selecionado, setSelecionado] = useState<Automovel | null>(null)
  const [dataInicio, setDataInicio] = useState('')
  const [dataFim, setDataFim] = useState('')
  const [enviando, setEnviando] = useState(false)

  const carregar = useCallback(async () => {
    setCarregando(true)
    try {
      setCarros(await request<Automovel[]>('/api/automoveis/disponiveis'))
      setErro('')
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar os carros.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => { void carregar() }, [carregar])

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase()
    if (!termo) return carros
    return carros.filter(c =>
      `${c.marca} ${c.modelo} ${c.placa} ${c.ano}`.toLowerCase().includes(termo)
    )
  }, [busca, carros])

  const dias = dataInicio && dataFim && dataFim >= dataInicio
    ? Math.round((Date.parse(dataFim) - Date.parse(dataInicio)) / 86400000) + 1
    : 0

  function alugar(carro: Automovel) {
    setErro(''); setAviso('')
    setSelecionado(carro)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function confirmarPedido(event: FormEvent) {
    event.preventDefault()
    if (!selecionado) return
    setErro(''); setAviso(''); setEnviando(true)
    try {
      await request('/api/alugueis', {
        method: 'POST',
        body: { automovelId: selecionado.id, dataInicio, dataFim },
      })
      setAviso(`Pedido criado para ${selecionado.marca} ${selecionado.modelo}. Aguardando aceite da empresa.`)
      setSelecionado(null)
      setDataInicio(''); setDataFim('')
      await carregar()
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível criar o pedido.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="home">
      <Topo />

      <div className="pagina-automoveis">
        <div className="cabecalho-pagina">
          <div>
            <p className="eyebrow">CATÁLOGO</p>
            <h1>Carros disponíveis</h1>
            <p className="subtitulo">Escolha um veículo para iniciar seu pedido de aluguel.</p>
          </div>
          <span className="contador">
            {filtrados.length} {filtrados.length === 1 ? 'veículo' : 'veículos'}
          </span>
        </div>

        <section className="painel-automoveis">
          <label className="campo">
            <span>Buscar</span>
            <input
              value={busca}
              onChange={e => setBusca(e.target.value)}
              placeholder="Marca, modelo ou placa"
            />
          </label>

          {selecionado && (
            <form onSubmit={confirmarPedido} className="form-pedido">
              <h2>Pedido: {selecionado.marca} {selecionado.modelo}</h2>
              <p className="subtitulo">
                {moeda(selecionado.valorDiaria)} por dia
                {dias > 0 && <> · {dias} {dias === 1 ? 'dia' : 'dias'} = <strong>{moeda(selecionado.valorDiaria * dias)}</strong></>}
              </p>
              <div className="grade-form">
                <label className="campo">
                  <span>Data de início</span>
                  <input type="date" required min={hojeIso()} value={dataInicio} onChange={e => setDataInicio(e.target.value)} />
                </label>
                <label className="campo">
                  <span>Data de devolução</span>
                  <input type="date" required min={dataInicio || hojeIso()} value={dataFim} onChange={e => setDataFim(e.target.value)} />
                </label>
              </div>
              <div className="botoes-form">
                <button className="btn" disabled={enviando}>
                  {enviando ? 'Enviando...' : 'Confirmar pedido'}
                </button>
                <button type="button" className="btn-secundario" onClick={() => setSelecionado(null)}>
                  Cancelar
                </button>
              </div>
            </form>
          )}

          {erro && <p className="alerta erro">{erro}</p>}
          {aviso && <p className="alerta sucesso">{aviso}</p>}
        </section>

        <CarList carros={filtrados} carregando={carregando} onAlugar={alugar} />
      </div>
    </main>
  )
}