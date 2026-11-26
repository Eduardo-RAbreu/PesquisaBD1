export default function SeloTipo({ nome }) {
  const ehTexto = nome.toLowerCase().includes('texto')
  return <span className={`selo ${ehTexto ? 'selo-texto' : 'selo-escala'}`}>{nome}</span>
}
