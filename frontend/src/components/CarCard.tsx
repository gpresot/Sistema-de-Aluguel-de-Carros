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
          : <span className="sem-foto">Sem foto</span>}
      </div>
      <div className="car-card-corpo">
        <div className="car-card-titulo">
          <h3>{carro.marca} {carro.modelo}</h3>
          <span className="car-card-ano">{carro.ano}</span>
        </div>
        <p className="car-card-placa"><span className="placa">{carro.placa}</span> {carro.proprietarioNome}</p>
        <div className="car-card-rodape">
          <p className="car-card-preco">{moeda(carro.valorDiaria)} <small>por dia</small></p>
          <button className="btn btn-compacto" onClick={() => onAlugar(carro)}>
            Alugar
          </button>
        </div>
      </div>
    </article>
  )
}
