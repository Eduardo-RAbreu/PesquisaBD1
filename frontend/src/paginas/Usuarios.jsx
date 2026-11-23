import { useState } from 'react'
import { api } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

export default function Usuarios() {
  const { dados: usuarios, erro: erroUsuarios, carregando, recarregar } = useDados('/api/usuarios')
  const { dados: grupos, recarregar: recarregarGrupos } = useDados('/api/grupos')
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [idsGrupos, setIdsGrupos] = useState([])
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')

  function alternarGrupo(idGrupo) {
    setIdsGrupos((atuais) =>
      atuais.includes(idGrupo) ? atuais.filter((x) => x !== idGrupo) : [...atuais, idGrupo],
    )
  }

  async function adicionar(evento) {
    evento.preventDefault()
    setErro('')
    setMensagem('')
    try {
      await api.post('/api/usuarios', { nome, email, idsGrupos })
      setNome('')
      setEmail('')
      setIdsGrupos([])
      setMensagem('Usuário cadastrado.')
      recarregar()
      recarregarGrupos()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function excluir(usuario) {
    if (!window.confirm(`Excluir o usuário "${usuario.nome}"?`)) return
    setErro('')
    setMensagem('')
    try {
      await api.delete(`/api/usuarios/${usuario.id}`)
      setMensagem('Usuário excluído.')
      recarregar()
      recarregarGrupos()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <h1>Usuários</h1>
      <p className="subtitulo">
        As pessoas que podem responder pesquisas. O e-mail é usado para entrar no sistema.
      </p>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro || erroUsuarios}</Aviso>

      <form className="formulario formulario-largo formulario-fora" onSubmit={adicionar}>
        <h2 className="secao">Novo usuário</h2>
        <div className="grade-dois">
          <div>
            <label htmlFor="nomeUsuario">Nome</label>
            <input
              id="nomeUsuario"
              type="text"
              maxLength={120}
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="emailUsuario">E-mail</label>
            <input
              id="emailUsuario"
              type="email"
              maxLength={150}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
        </div>

        <span className="rotulo">Grupos</span>
        {grupos && grupos.length === 0 && (
          <p className="dica">Cadastre um grupo na página Grupos para ligar o usuário a ele.</p>
        )}
        <div className="grade-opcoes">
          {(grupos ?? []).map((g) => (
            <label className="opcao" key={g.id}>
              <input
                type="checkbox"
                checked={idsGrupos.includes(g.id)}
                onChange={() => alternarGrupo(g.id)}
              />
              <span className="opcao-texto"><span>{g.nomeGrupo}</span></span>
            </label>
          ))}
        </div>

        <div className="botoes">
          <button type="submit" className="botao">Adicionar usuário</button>
        </div>
      </form>

      {carregando && !usuarios && <p className="vazio">Carregando...</p>}
      {usuarios && usuarios.length === 0 && <p className="vazio">Nenhum usuário cadastrado ainda.</p>}

      {usuarios && usuarios.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Grupos</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id}>
                  <td>{u.nome}</td>
                  <td>{u.email}</td>
                  <td className="detalhe">{u.grupos}</td>
                  <td className="acoes">
                    <button type="button" className="link-perigo" onClick={() => excluir(u)}>
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )
}
