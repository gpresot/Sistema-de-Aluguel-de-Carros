/** Marca da locadora: monograma no formato de placa + nome. */
export default function Logo() {
  return (
    <span className="logo">
      <span className="placa logo-placa" aria-hidden="true">PC</span>
      <span className="logo-nome">Presot Clássicos</span>
    </span>
  )
}
