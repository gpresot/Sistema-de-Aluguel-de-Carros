export const moeda = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export const dataBr = (iso: string) => {
  const [a, m, d] = iso.slice(0, 10).split('-')
  return `${d}/${m}/${a}`
}

export const hojeIso = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

export const ROTULO_STATUS: Record<string, string> = {
  PENDENTE: 'Aguardando empresa',
  EM_ANALISE: 'Em análise financeira',
  APROVADO: 'Aprovado',
  REJEITADO: 'Rejeitado',
  CONTRATADO: 'Contrato executado',
  CANCELADO: 'Cancelado',
}
