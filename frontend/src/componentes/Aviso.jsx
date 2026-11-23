// Mostra erro ou sucesso
export default function Aviso({ tipo, children }) {
  if (!children) return null
  return (
    <p className={`aviso ${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'}>
      {children}
    </p>
  )
}
