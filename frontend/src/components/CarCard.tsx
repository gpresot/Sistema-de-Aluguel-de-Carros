import type { Automovel } from '../types/automovel'

type Props = {
  carro: Automovel
  onAlugar: (carro: Automovel) => void
}

export default function CarCard({ carro, onAlugar }: Props) {
  return (
    <article className="car-card">
      <div className="car-card-imagem" aria-hidden="true">🚗</div>
      <div className="car-card-corpo">
        <p className="eyebrow">{carro.ano}</p>
        <h3>{carro.marca} {carro.modelo}</h3>
        <p className="car-card-placa">Placa {carro.placa}</p>
        <button className="btn" onClick={() => onAlugar(carro)}>
          Alugar
        </button>
      </div>
    </article>
  )
}