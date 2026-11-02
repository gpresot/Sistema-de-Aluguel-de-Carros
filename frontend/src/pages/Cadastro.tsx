import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api'
import { useAuth } from '../auth/context'
import AuthLayout from '../components/AuthLayout'
import PasswordField from '../components/PasswordField'
import TextField from '../components/TextField'

const MAX_RENDIMENTOS = 3

interface RendimentoForm {
  entidadeEmpregadora: string
  valor: string
}

export default function Cadastro() {
  const { registrar } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmacaoSenha, setConfirmacaoSenha] = useState('')
  const [nome, setNome] = useState('')
  const [rg, setRg] = useState('')
  const [cpf, setCpf] = useState('')
  const [endereco, setEndereco] = useState('')
  const [profissao, setProfissao] = useState('')
  const [rendimentos, setRendimentos] = useState<RendimentoForm[]>([])
  const [erro, setErro] = useState('')
  const [campos, setCampos] = useState<Record<string, string>>({})
  const [enviando, setEnviando] = useState(false)

  function atualizarRendimento(i: number, parcial: Partial<RendimentoForm>) {
    setRendimentos((lista) => lista.map((r, idx) => (idx === i ? { ...r, ...parcial } : r)))
  }

  function validar(): string {
    if (senha.length < 8) return 'A senha deve possuir pelo menos 8 caracteres.'
    if (new TextEncoder().encode(senha).length > 72) return 'A senha deve ter no máximo 72 bytes.'
    if (senha !== confirmacaoSenha) return 'As senhas informadas não coincidem.'
    if (!/^\d{11}$/.test(cpf.replace(/\D/g, ''))) return 'CPF deve ter 11 dígitos.'
    for (const r of rendimentos) {
      if (!(Number(r.valor.replace(',', '.')) > 0)) return 'Informe um valor de rendimento maior que zero.'
    }
    return ''
  }

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setCampos({})
    const problema = validar()
    if (problema) {
      setErro(problema)
      return
    }
    setErro('')
    setEnviando(true)
    try {
      await registrar({
        email,
        senha,
        confirmacaoSenha,
        nome,
        rg,
        cpf: cpf.replace(/\D/g, ''),
        endereco,
        profissao,
        rendimentos: rendimentos.map((r) => ({
          entidadeEmpregadora: r.entidadeEmpregadora,
          valor: Number(r.valor.replace(',', '.')),
        })),
      })
      navigate('/', { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setErro(err.message)
        setCampos(err.campos ?? {})
      } else {
        setErro('Erro inesperado.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <AuthLayout titulo="Criar conta" subtitulo="Cadastro de cliente" largo>
      {erro && <p className="alerta erro" role="alert">{erro}</p>}
      <form onSubmit={enviar}>
        <TextField id="nome" label="Nome completo" autoComplete="name" value={nome} onChange={setNome} erro={campos.nome} />
        <TextField id="email" label="E-mail" type="email" autoComplete="email" value={email} onChange={setEmail} erro={campos.email} />
        <div className="linha">
          <PasswordField id="senha" label="Senha" autoComplete="new-password" value={senha} onChange={setSenha} erro={campos.senha} />
          <PasswordField id="confirmacaoSenha" label="Confirmar senha" autoComplete="new-password" value={confirmacaoSenha} onChange={setConfirmacaoSenha} />
        </div>
        <div className="linha">
          <TextField id="rg" label="RG" value={rg} onChange={setRg} erro={campos.rg} />
          <TextField id="cpf" label="CPF" inputMode="numeric" placeholder="somente números" value={cpf} onChange={setCpf} erro={campos.cpf} />
        </div>
        <TextField id="endereco" label="Endereço" autoComplete="street-address" value={endereco} onChange={setEndereco} erro={campos.endereco} />
        <TextField id="profissao" label="Profissão" value={profissao} onChange={setProfissao} erro={campos.profissao} />

        <fieldset className="rendimentos">
          <legend>Rendimentos (máximo {MAX_RENDIMENTOS})</legend>
          {rendimentos.map((r, i) => (
            <div className="linha rendimento" key={i}>
              <TextField
                id={`ent-${i}`}
                label="Entidade empregadora"
                value={r.entidadeEmpregadora}
                onChange={(v) => atualizarRendimento(i, { entidadeEmpregadora: v })}
              />
              <TextField
                id={`val-${i}`}
                label="Valor (R$)"
                inputMode="decimal"
                value={r.valor}
                onChange={(v) => atualizarRendimento(i, { valor: v })}
              />
              <button
                type="button"
                className="link-btn remover"
                onClick={() => setRendimentos((l) => l.filter((_, idx) => idx !== i))}
              >
                Remover
              </button>
            </div>
          ))}
          {rendimentos.length < MAX_RENDIMENTOS && (
            <button
              type="button"
              className="btn-secundario"
              onClick={() => setRendimentos((l) => [...l, { entidadeEmpregadora: '', valor: '' }])}
            >
              + Adicionar rendimento
            </button>
          )}
        </fieldset>

        <button className="btn" type="submit" disabled={enviando}>
          {enviando ? 'Criando conta...' : 'Criar conta'}
        </button>
      </form>
      <p className="links">
        <Link to="/login">Já tenho conta</Link>
      </p>
    </AuthLayout>
  )
}
