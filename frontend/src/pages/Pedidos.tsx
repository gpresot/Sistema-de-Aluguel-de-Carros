import { useCallback, useEffect, useState } from 'react'
import { ApiError, request, urlApi } from '../api'
import { useAuth } from '../auth/context'
import Topo from '../components/Topo'
import { ROTULO_STATUS, dataBr, hojeIso, moeda } from '../format'
import type { Aluguel, Perfil } from '../types'

type CardProps = {
  pedido: Aluguel
  perfil: Perfil
  onMudou: (mensagem: string) => Promise<void>
  onErro: (mensagem: string) => void
}

function PedidoCard({ pedido, perfil, onMudou, onErro }: CardProps) {
  const [ocupado, setOcupado] = useState(false)
  const [motivo, setMotivo] = useState('')
  const [editando, setEditando] = useState(false)
  const [inicio, setInicio] = useState(pedido.dataInicio)
  const [fim, setFim] = useState(pedido.dataFim)

  async function executar(caminho: string, metodo: 'PUT' | 'DELETE', body: unknown, ok: string) {
    setOcupado(true)
    try {
      await request<Aluguel>(`/api/alugueis/${pedido.id}${caminho}`, { method: metodo, body })
      setMotivo(''); setEditando(false)
      await onMudou(ok)
    } catch (e) {
      onErro(e instanceof ApiError ? e.message : 'Não foi possível concluir a ação.')
    } finally {
      setOcupado(false)
    }
  }

  const carro = pedido.automovel
  const a = pedido.analise
  const cliente = perfil === 'CLIENTE'
  const empresa = perfil === 'EMPRESA'
  const banco = perfil === 'BANCO'

  return (
    <article className="pedido-card">
      <div className="pedido-topo">
        <div>
          <h3>#{pedido.id} · {carro.marca} {carro.modelo} ({carro.ano})</h3>
          <p className="pedido-meta">
            {dataBr(pedido.dataInicio)} a {dataBr(pedido.dataFim)} · {pedido.dias} {pedido.dias === 1 ? 'dia' : 'dias'} · <strong>{moeda(pedido.valorTotal)}</strong>
          </p>
          {!cliente && <p className="pedido-meta">Cliente: {pedido.clienteNome} · Placa {carro.placa}</p>}
        </div>
        <span className={`selo ${pedido.status}`}>{ROTULO_STATUS[pedido.status] ?? pedido.status}</span>
      </div>

      <div className="pedido-corpo">
        <div className="pedido-foto">
          {carro.fotoUrl ? <img src={urlApi(carro.fotoUrl)} alt={`${carro.marca} ${carro.modelo}`} loading="lazy" /> : <span aria-hidden="true">🚗</span>}
        </div>
        <div>
          {pedido.status === 'PENDENTE' && cliente && <p className="pedido-meta">A empresa ainda vai aceitar ou recusar o seu pedido.</p>}
          {pedido.status === 'EM_ANALISE' && cliente && <p className="pedido-meta">A empresa aceitou. O banco está analisando sua renda.</p>}
          {pedido.status === 'APROVADO' && cliente && <p className="pedido-meta">Pedido aprovado pelo banco{pedido.avaliadorNome ? ` (${pedido.avaliadorNome})` : ''}.</p>}
          {pedido.motivo && <p className="pedido-meta">Motivo: {pedido.motivo}{pedido.avaliadorNome ? ` — ${pedido.avaliadorNome}` : ''}</p>}
        </div>
      </div>

      {banco && a && (pedido.status === 'EM_ANALISE') && (
        <p className={`analise-renda ${a.aprovada ? 'positiva' : 'negativa'}`}>
          <strong>Análise de renda</strong>
          <span>Renda mensal informada: {moeda(a.rendaMensal)}</span>
          <span>Limite (30% da renda): {moeda(a.limiteParcela)}</span>
          <span>Comprometimento do aluguel: {moeda(a.parcelaMensal)}</span>
          <span>{a.aprovada ? 'Dentro do limite: pode ser aprovado.' : 'Acima do limite (ou sem renda informada): não pode ser aprovado.'}</span>
        </p>
      )}

      <div className="acoes-pedido">
        {cliente && pedido.status === 'PENDENTE' && !editando && (
          <button className="btn-secundario" disabled={ocupado} onClick={() => setEditando(true)}>Alterar datas</button>
        )}
        {cliente && editando && (
          <>
            <input type="date" min={hojeIso()} value={inicio} onChange={e => setInicio(e.target.value)} />
            <input type="date" min={inicio} value={fim} onChange={e => setFim(e.target.value)} />
            <button className="btn" disabled={ocupado} onClick={() => void executar('/datas', 'PUT', { dataInicio: inicio, dataFim: fim }, 'Datas atualizadas.')}>Salvar</button>
            <button className="btn-secundario" onClick={() => setEditando(false)}>Voltar</button>
          </>
        )}
        {cliente && ['PENDENTE', 'EM_ANALISE', 'APROVADO'].includes(pedido.status) && !editando && (
          <button className="btn-perigo" disabled={ocupado}
            onClick={() => { if (window.confirm('Cancelar este pedido?')) void executar('', 'DELETE', undefined, 'Pedido cancelado.') }}>
            Cancelar pedido
          </button>
        )}

        {empresa && pedido.status === 'PENDENTE' && (
          <>
            <button className="btn" disabled={ocupado} onClick={() => void executar('/aceitar', 'PUT', undefined, 'Pedido aceito e enviado ao banco.')}>Aceitar pedido</button>
            <input type="text" placeholder="Motivo da recusa (opcional)" value={motivo} onChange={e => setMotivo(e.target.value)} />
            <button className="btn-perigo" disabled={ocupado} onClick={() => void executar('/recusar', 'PUT', { motivo }, 'Pedido recusado.')}>Recusar</button>
          </>
        )}

        {banco && pedido.status === 'EM_ANALISE' && (
          <>
            <button className="btn" disabled={ocupado || !a?.aprovada}
              title={a?.aprovada ? '' : 'A renda não comporta este aluguel'}
              onClick={() => void executar('/parecer', 'PUT', { aprovar: true, motivo }, 'Pedido aprovado.')}>Aprovar</button>
            <input type="text" placeholder="Motivo (obrigatório para reprovar)" value={motivo} onChange={e => setMotivo(e.target.value)} />
            <button className="btn-perigo" disabled={ocupado || !motivo.trim()}
              onClick={() => void executar('/parecer', 'PUT', { aprovar: false, motivo }, 'Pedido reprovado.')}>Reprovar</button>
          </>
        )}
      </div>
    </article>
  )
}

export default function Pedidos() {
  const { sessao } = useAuth()
  const [lista, setLista] = useState<Aluguel[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [filtro, setFiltro] = useState('TODOS')

  const perfil = sessao?.perfil
  const caminho = perfil === 'CLIENTE' ? '/api/alugueis/meus' : '/api/alugueis/agente'

  const carregar = useCallback(async () => {
    setCarregando(true)
    try { setLista(await request<Aluguel[]>(caminho)); setErro('') }
    catch (e) { setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar os pedidos.') }
    finally { setCarregando(false) }
  }, [caminho])

  useEffect(() => { void carregar() }, [carregar])

  async function aposAcao(mensagem: string) {
    setErro(''); setSucesso(mensagem)
    await carregar()
  }

  if (!perfil) return null
  const visiveis = filtro === 'TODOS' ? lista : lista.filter(p => p.status === filtro)
  const titulo = perfil === 'CLIENTE' ? 'Meus pedidos' : perfil === 'EMPRESA' ? 'Pedidos dos meus carros' : 'Análise financeira'

  return (
    <main className="home">
      <Topo />
      <div className="pagina-automoveis">
        <div className="cabecalho-pagina">
          <div>
            <p className="eyebrow">PEDIDOS DE ALUGUEL</p>
            <h1>{titulo}</h1>
            <p className="subtitulo">
              {perfil === 'CLIENTE' && 'Acompanhe o status de cada pedido.'}
              {perfil === 'EMPRESA' && 'Aceite um pedido para enviá-lo ao banco, ou recuse.'}
              {perfil === 'BANCO' && 'Confira a renda do cliente e aprove ou reprove os pedidos aceitos pelas empresas.'}
            </p>
          </div>
          <span className="contador">{visiveis.length} {visiveis.length === 1 ? 'pedido' : 'pedidos'}</span>
        </div>

        <div className="acoes-pedido" style={{ marginBottom: 16 }}>
          <select value={filtro} onChange={e => setFiltro(e.target.value)} aria-label="Filtrar por status">
            <option value="TODOS">Todos os status</option>
            {Object.entries(ROTULO_STATUS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
          </select>
          <button className="btn-secundario" onClick={() => void carregar()}>Atualizar</button>
        </div>

        {erro && <p className="alerta erro">{erro}</p>}
        {sucesso && <p className="alerta sucesso">{sucesso}</p>}

        {carregando ? <p className="estado-vazio">Carregando pedidos...</p>
          : visiveis.length === 0 ? <p className="estado-vazio">Nenhum pedido por aqui.</p>
          : <div className="lista-pedidos">
            {visiveis.map(p => (
              <PedidoCard key={p.id} pedido={p} perfil={perfil}
                onMudou={aposAcao} onErro={m => { setSucesso(''); setErro(m) }} />
            ))}
          </div>}
      </div>
    </main>
  )
}
