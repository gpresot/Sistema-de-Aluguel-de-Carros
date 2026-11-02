import { urlApi } from '../api'
import { moeda } from '../format'
import type { Automovel } from '../types/automovel'

type Props = {
  carro: Automovel
  onAlugar: (carro: Automovel) => void
}

export default function CarCard({ carro, onAlugar }: Props) {
  return (
    <article className="car-card">
      <div className="car-card-imagem">
        {carro.fotoUrl
          ? <img src={urlApi(carro.fotoUrl)} alt={`${carro.marca} ${carro.modelo}`} loading="lazy" />
          : <span aria-hidden="true">🚗</span>}
      </div>
      <div className="car-card-corpo">
        <p className="eyebrow">{carro.ano}</p>
        <h3>{carro.marca} {carro.modelo}</h3>
        <p className="car-card-placa">Placa {carro.placa} · {carro.proprietarioNome}</p>
        <p className="car-card-preco">{moeda(carro.valorDiaria)} <small>/ dia</small></p>
        <button className="btn" onClick={() => onAlugar(carro)}>
          Alugar
        </button>
      </div>
    </article>
  )
}
