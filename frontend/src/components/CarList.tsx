import CarCard from './CarCard'
import type { Automovel } from '../types/automovel'

type Props = {
  carros: Automovel[]
  carregando: boolean
  onAlugar: (carro: Automovel) => void
}

export default function CarList({ carros, carregando, onAlugar }: Props) {
  if (carregando) {
    return <p className="estado-vazio">Carregando carros...</p>
  }

  if (carros.length === 0) {
    return <p className="estado-vazio">Nenhum carro disponível no momento.</p>
  }

  return (
    <div className="car-grid">
      {carros.map(carro => (
        <CarCard key={carro.id} carro={carro} onAlugar={onAlugar} />
      ))}
    </div>
  )
}