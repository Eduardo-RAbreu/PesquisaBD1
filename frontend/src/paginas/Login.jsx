import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import Aviso from '../componentes/Aviso.jsx'

// Login simples apenas com e-mail de um usuário cadastrado
export default function Login() {
  const { usuario, entrar } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await entrar(email)
      navigate('/responder')
    } catch (e) {
      setErro(e.message)
    } finally {
      setEnviando(false)
    }
  }

  if (usuario) {
    return (
      <>
        <h1>Entrar</h1>
        <p className="subtitulo">
          Você já está logado como <strong>{usuario.nome}</strong>.{' '}
          <Link to="/responder">Ir para as pesquisas</Link>
        </p>
      </>
    )
  }

  return (
    <>
      <h1>Entrar</h1>
      <p className="subtitulo">
        Informe o e-mail de um usuário cadastrado. Os e-mails estão na página <Link to="/usuarios">Usuários</Link>.
      </p>
      <Aviso tipo="erro">{erro}</Aviso>

      <form className="formulario" onSubmit={enviar}>
        <label htmlFor="email">E-mail</label>
        <input
          id="email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <div className="botoes">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Entrando...' : 'Entrar'}
          </button>
        </div>
      </form>
    </>
  )
}
