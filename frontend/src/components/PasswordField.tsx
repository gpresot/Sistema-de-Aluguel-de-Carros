import { useState } from 'react'

interface Props {
  id: string
  label: string
  value: string
  onChange: (valor: string) => void
  autoComplete: 'current-password' | 'new-password'
  erro?: string
}

export default function PasswordField({ id, label, value, onChange, autoComplete, erro }: Props) {
  const [visivel, setVisivel] = useState(false)
  return (
    <div className="campo">
      <label htmlFor={id}>{label}</label>
      <div className="senha-wrap">
        <input
          id={id}
          type={visivel ? 'text' : 'password'}
          value={value}
          autoComplete={autoComplete}
          onChange={(e) => onChange(e.target.value)}
          required
        />
        <button
          type="button"
          className="link-btn"
          aria-pressed={visivel}
          onClick={() => setVisivel((v) => !v)}
        >
          {visivel ? 'Ocultar' : 'Mostrar'}
        </button>
      </div>
      {erro && <span className="erro-campo">{erro}</span>}
    </div>
  )
}
