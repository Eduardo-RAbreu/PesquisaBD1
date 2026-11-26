import { useState } from 'react'
import { api } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

export default function Grupos() {
  const { dados: grupos, erro: erroCarga, carregando, recarregar } = useDados('/api/grupos')
  const [nomeGrupo, setNomeGrupo] = useState('')
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')

  async function adicionar(evento) {
    evento.preventDefault()
    setErro('')
    setMensagem('')
    try {
      await api.post('/api/grupos', { nomeGrupo })
      setNomeGrupo('')
      setMensagem('Grupo cadastrado.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function excluir(grupo) {
    if (!window.confirm(`Excluir o grupo "${grupo.nomeGrupo}"?`)) return
    setErro('')
    setMensagem('')
    try {
      await api.delete(`/api/grupos/${grupo.id}`)
      setMensagem('Grupo excluído.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <h1>Grupos</h1>
      <p className="subtitulo">
        Turmas ou categorias de pessoas. Cada pesquisa é indicada para um ou mais grupos.
      </p>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro || erroCarga}</Aviso>

      <form className="linha-form" onSubmit={adicionar}>
        <label className="so-leitor" htmlFor="nomeGrupo">Nome do novo grupo</label>
        <input
          id="nomeGrupo"
          type="text"
          maxLength={100}
          placeholder="Nome do novo grupo"
          value={nomeGrupo}
          onChange={(e) => setNomeGrupo(e.target.value)}
          required
        />
        <button type="submit" className="botao">Adicionar grupo</button>
      </form>

      {carregando && !grupos && <p className="vazio">Carregando...</p>}
      {grupos && grupos.length === 0 && <p className="vazio">Nenhum grupo cadastrado ainda.</p>}

      {grupos && grupos.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Grupo</th>
                <th>Usuários</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {grupos.map((g) => (
                <tr key={g.id}>
                  <td>{g.nomeGrupo}</td>
                  <td>{g.totalUsuarios}</td>
                  <td className="acoes">
                    <button type="button" className="link-perigo" onClick={() => excluir(g)}>
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
