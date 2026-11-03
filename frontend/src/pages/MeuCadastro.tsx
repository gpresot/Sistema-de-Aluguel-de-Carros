import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError, request } from '../api'
import { useAuth } from '../auth/context'
import Topo from '../components/Topo'
import TextField from '../components/TextField'
import type { Cliente } from '../types'

const MAX_RENDIMENTOS = 3

interface RendimentoForm {
  entidadeEmpregadora: string
  valor: string
}

/** CRUD do cliente: consulta, alteração e exclusão do próprio cadastro (o cadastro inicial é a tela Criar conta). */
export default function MeuCadastro() {
  const { logout } = useAuth()
  const [email, setEmail] = useState('')
  const [nome, setNome] = useState('')
  const [rg, setRg] = useState('')
  const [cpf, setCpf] = useState('')
  const [endereco, setEndereco] = useState('')
  const [profissao, setProfissao] = useState('')
  const [rendimentos, setRendimentos] = useState<RendimentoForm[]>([])
  const [carregando, setCarregando] = useState(true)
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [campos, setCampos] = useState<Record<string, string>>({})

  function preencher(c: Cliente) {
    setEmail(c.email)
    setNome(c.nome)
    setRg(c.rg)
    setCpf(c.cpf)
    setEndereco(c.endereco)
    setProfissao(c.profissao)
    setRendimentos(c.rendimentos.map((r) => ({
      entidadeEmpregadora: r.entidadeEmpregadora,
      valor: String(r.valor),
    })))
  }

  useEffect(() => {
    let cancelado = false
    request<Cliente>('/api/clientes/me')
      .then((c) => { if (!cancelado) preencher(c) })
      .catch((e) => { if (!cancelado) setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar o cadastro.') })
      .finally(() => { if (!cancelado) setCarregando(false) })
    return () => { cancelado = true }
  }, [])

  function atualizarRendimento(i: number, parcial: Partial<RendimentoForm>) {
    setRendimentos((lista) => lista.map((r, idx) => (idx === i ? { ...r, ...parcial } : r)))
  }

  async function salvar(event: FormEvent) {
    event.preventDefault()
    setErro(''); setSucesso(''); setCampos({})
    for (const r of rendimentos) {
      if (!(Number(r.valor.replace(',', '.')) > 0)) {
        setErro('Informe um valor de rendimento maior que zero.')
        return
      }
    }
    setEnviando(true)
    try {
      const atualizado = await request<Cliente>('/api/clientes/me', {
        method: 'PUT',
        body: {
          nome, rg, cpf: cpf.replace(/\D/g, ''), endereco, profissao,
          rendimentos: rendimentos.map((r) => ({
            entidadeEmpregadora: r.entidadeEmpregadora,
            valor: Number(r.valor.replace(',', '.')),
          })),
        },
      })
      preencher(atualizado)
      setSucesso('Cadastro atualizado.')
    } catch (e) {
      if (e instanceof ApiError) {
        setErro(e.message)
        setCampos(e.campos ?? {})
      } else {
        setErro('Erro inesperado.')
      }
    } finally {
      setEnviando(false)
    }
  }

  async function excluir() {
    if (!window.confirm('Excluir seu cadastro? Esta ação não pode ser desfeita.')) return
    setErro(''); setSucesso('')
    try {
      await request<void>('/api/clientes/me', { method: 'DELETE' })
      logout()
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível excluir o cadastro.')
    }
  }

  return (
    <main className="home">
      <Topo />
      <section className="card card-largo">
        <h1>Meu cadastro</h1>
        <p className="subtitulo">Consulte e atualize seus dados. E-mail: {email || '...'}</p>
        {erro && <p className="alerta erro" role="alert">{erro}</p>}
        {sucesso && <p className="alerta sucesso">{sucesso}</p>}
        {carregando ? <p className="estado-vazio">Carregando...</p> : (
          <form onSubmit={salvar}>
            <TextField id="nome" label="Nome completo" value={nome} onChange={setNome} erro={campos.nome} />
            <div className="linha">
              <TextField id="rg" label="RG" value={rg} onChange={setRg} erro={campos.rg} />
              <TextField id="cpf" label="CPF" inputMode="numeric" placeholder="somente números" value={cpf} onChange={setCpf} erro={campos.cpf} />
            </div>
            <TextField id="endereco" label="Endereço" value={endereco} onChange={setEndereco} erro={campos.endereco} />
            <TextField id="profissao" label="Profissão" value={profissao} onChange={setProfissao} erro={campos.profissao} />

            <fieldset className="rendimentos">
              <legend>Rendimentos (máximo {MAX_RENDIMENTOS})</legend>
              {rendimentos.map((r, i) => (
                <div className="linha rendimento" key={i}>
                  <TextField id={`ent-${i}`} label="Entidade empregadora" value={r.entidadeEmpregadora}
                    onChange={(v) => atualizarRendimento(i, { entidadeEmpregadora: v })} />
                  <TextField id={`val-${i}`} label="Valor (R$)" inputMode="decimal" value={r.valor}
                    onChange={(v) => atualizarRendimento(i, { valor: v })} />
                  <button type="button" className="link-btn remover"
                    onClick={() => setRendimentos((l) => l.filter((_, idx) => idx !== i))}>
                    Remover
                  </button>
                </div>
              ))}
              {rendimentos.length < MAX_RENDIMENTOS && (
                <button type="button" className="btn-secundario"
                  onClick={() => setRendimentos((l) => [...l, { entidadeEmpregadora: '', valor: '' }])}>
                  + Adicionar rendimento
                </button>
              )}
            </fieldset>

            <div className="botoes-form">
              <button className="btn" type="submit" disabled={enviando}>
                {enviando ? 'Salvando...' : 'Salvar alterações'}
              </button>
              <button type="button" className="btn-perigo" onClick={() => void excluir()}>
                Excluir cadastro
              </button>
            </div>
          </form>
        )}
      </section>
    </main>
  )
}
