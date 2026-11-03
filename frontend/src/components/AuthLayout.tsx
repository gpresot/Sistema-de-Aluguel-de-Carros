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
      <section className={largo ? 'auth-conteudo auth-largo' : 'auth-conteudo'}>
        <Logo />
        <h1>{titulo}</h1>
        {subtitulo && <p className="subtitulo">{subtitulo}</p>}
        {children}
      </section>
    </main>
  )
}