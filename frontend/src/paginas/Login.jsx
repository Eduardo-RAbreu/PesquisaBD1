import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'
import Aviso from '../componentes/Aviso.jsx'

export default function Login() {
  const { usuario, entrar } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await entrar(email, senha)
      navigate('/', { replace: true })
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
          <Link to="/">Ir para o início</Link>
        </p>
      </>
    )
  }

  return (
    <>
      <h1>Entrar</h1>
      <p className="subtitulo">
        Informe o e-mail e a senha de um usuário cadastrado.
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
        <label htmlFor="senha">Senha</label>
        <input
          id="senha"
          type="password"
          autoComplete="current-password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
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
