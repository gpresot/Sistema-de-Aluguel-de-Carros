import type { ReactNode } from 'react'
import Logo from './Logo'

interface Props {
  titulo: string
  subtitulo?: string
  largo?: boolean
  children: ReactNode
}

export default function AuthLayout({ titulo, subtitulo, largo, children }: Props) {
  return (
    <main className="auth-page">
      <aside className="auth-marca">
        <Logo />
        <p>Carros clássicos, do pedido ao contrato.</p>
      </aside>
      <div className="auth-lado">
        <section className={largo ? 'auth-conteudo auth-largo' : 'auth-conteudo'}>
          <h1>{titulo}</h1>
          {subtitulo && <p className="subtitulo">{subtitulo}</p>}
          {children}
        </section>
      </div>
    </main>
  )
}
