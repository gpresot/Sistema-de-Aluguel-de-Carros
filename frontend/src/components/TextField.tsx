interface Props {
  id: string
  label: string
  value: string
  onChange: (valor: string) => void
  type?: 'text' | 'email' | 'number'
  autoComplete?: string
  inputMode?: 'numeric' | 'decimal' | 'text'
  placeholder?: string
  erro?: string
}

export default function TextField({
  id,
  label,
  value,
  onChange,
  type = 'text',
  autoComplete,
  inputMode,
  placeholder,
  erro,
}: Props) {
  return (
    <div className="campo">
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        type={type}
        value={value}
        autoComplete={autoComplete}
        inputMode={inputMode}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        required
      />
      {erro && <span className="erro-campo">{erro}</span>}
    </div>
  )
}
