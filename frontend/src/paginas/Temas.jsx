import { useState } from 'react'
import { api } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

export default function Temas() {
  const { dados: temas, erro: erroCarga, carregando, recarregar } = useDados('/api/temas')
  const [nomeTema, setNomeTema] = useState('')
  const [descricao, setDescricao] = useState('')
  const [mensagem, setMensagem] = useState('')
  const [erro, setErro] = useState('')

  async function adicionar(evento) {
    evento.preventDefault()
    setErro('')
    setMensagem('')
    try {
      await api.post('/api/temas', { nomeTema, descricao })
      setNomeTema('')
      setDescricao('')
      setMensagem('Tema cadastrado.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  async function excluir(tema) {
    if (!window.confirm(`Excluir o tema "${tema.nomeTema}"?`)) return
    setErro('')
    setMensagem('')
    try {
      await api.delete(`/api/temas/${tema.id}`)
      setMensagem('Tema excluído.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <h1>Temas</h1>
      <p className="subtitulo">
        Os assuntos que agrupam as questões. Os relatórios comparam resultados por tema.
      </p>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro || erroCarga}</Aviso>

      <form className="linha-form" onSubmit={adicionar}>
        <label className="so-leitor" htmlFor="nomeTema">Nome do novo tema</label>
        <input
          id="nomeTema"
          type="text"
          maxLength={100}
          placeholder="Nome do novo tema"
          value={nomeTema}
          onChange={(e) => setNomeTema(e.target.value)}
          required
        />
        <label className="so-leitor" htmlFor="descricaoTema">Descrição do tema</label>
        <input
          id="descricaoTema"
          type="text"
          placeholder="Descrição (opcional)"
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
        <button type="submit" className="botao">Adicionar tema</button>
      </form>

      {carregando && !temas && <p className="vazio">Carregando...</p>}
      {temas && temas.length === 0 && <p className="vazio">Nenhum tema cadastrado ainda.</p>}

      {temas && temas.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Tema</th>
                <th>Descrição</th>
                <th>Questões</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {temas.map((t) => (
                <tr key={t.id}>
                  <td>{t.nomeTema}</td>
                  <td className="detalhe">{t.descricao}</td>
                  <td>{t.totalQuestoes}</td>
                  <td className="acoes">
                    <button type="button" className="link-perigo" onClick={() => excluir(t)}>
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
