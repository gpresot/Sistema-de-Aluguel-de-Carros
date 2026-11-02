import type { ReactNode } from 'react'

interface Props {
  titulo: string
  subtitulo?: string
  largo?: boolean
  children: ReactNode
}

export default function AuthLayout({ titulo, subtitulo, largo, children }: Props) {
  return (
    <main className="auth-page">
      <section className={largo ? 'card card-largo' : 'card'}>
        <h1>{titulo}</h1>
        {subtitulo && <p className="subtitulo">{subtitulo}</p>}
        {children}
      </section>
    </main>
  )
}
