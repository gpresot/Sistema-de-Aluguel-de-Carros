import type { Automovel } from './automovel'

export type StatusAluguel = 'PENDENTE' | 'EM_ANALISE' | 'APROVADO' | 'REJEITADO' | 'CANCELADO' | 'FINALIZADO'

export type AnaliseFinanceira = {
  rendaMensal: number
  parcelaMensal: number
  limiteParcela: number
  aprovada: boolean
}

export type Aluguel = {
  id: number
  status: StatusAluguel
  dataInicio: string
  dataFim: string
  dias: number
  valorTotal: number
  motivo: string | null
  avaliadorNome: string | null
  dataAvaliacao: string | null
  criadoEm: string | null
  clienteNome: string
  automovel: Automovel
  analise: AnaliseFinanceira | null
}
