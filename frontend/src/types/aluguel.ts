import type { Automovel } from './automovel'

export type StatusAluguel = 'PENDENTE' | 'EM_ANALISE' | 'APROVADO' | 'REJEITADO' | 'CONTRATADO' | 'CANCELADO'

export type AnaliseFinanceira = {
  rendaMensal: number
  parcelaMensal: number
  limiteParcela: number
  aprovada: boolean
}

export type ContratoCredito = {
  id: number
  bancoNome: string
  valorFinanciado: number
  taxaJurosMensal: number
  criadoEm: string | null
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
  contratoCredito: ContratoCredito | null
}
