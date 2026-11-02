export type Perfil = 'CLIENTE' | 'EMPRESA' | 'BANCO'


export interface AuthResponse {
  token: string
  email: string
  perfil: Perfil
  nome: string
}

export interface RendimentoInput {
  entidadeEmpregadora: string
  valor: number
}

export interface RegistroClienteInput {
  email: string
  senha: string
  confirmacaoSenha: string
  nome: string
  rg: string
  cpf: string
  endereco: string
  profissao: string
  rendimentos: RendimentoInput[]
}

export interface Mensagem {
  mensagem: string
}
