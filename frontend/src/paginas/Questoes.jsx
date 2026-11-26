import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'
import SeloTipo from '../componentes/SeloTipo.jsx'

export default function Questoes() {
  const location = useLocation()
  const { dados: questoes, erro: erroCarga, carregando, recarregar } = useDados('/api/questoes')
  // mensagem enviada pelo formulário depois de salvar (ex.: "Questão cadastrada.")
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? '')
  const [erro, setErro] = useState('')

  async function excluir(questao) {
    if (!window.confirm('Excluir esta questão?')) return
    setErro('')
    setMensagem('')
    try {
      await api.delete(`/api/questoes/${questao.id}`)
      setMensagem('Questão excluída.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Questões</h1>
          <p className="subtitulo">As perguntas que podem entrar nas pesquisas.</p>
        </div>
        <Link className="botao" to="/questoes/nova">Nova questão</Link>
      </div>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro || erroCarga}</Aviso>

      {carregando && !questoes && <p className="vazio">Carregando...</p>}
      {questoes && questoes.length === 0 && (
        <p className="vazio">Nenhuma questão cadastrada. Use "Nova questão" para cadastrar a primeira.</p>
      )}

      {questoes && questoes.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Enunciado</th>
                <th>Tema</th>
                <th>Tipo</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {questoes.map((q) => (
                <tr key={q.id}>
                  <td>{q.descricao}</td>
                  <td>{q.nomeTema}</td>
                  <td><SeloTipo nome={q.nomeTipo} /></td>
                  <td className="acoes">
                    <Link to={`/questoes/${q.id}/editar`}>Editar</Link>
                    <button type="button" className="link-perigo" onClick={() => excluir(q)}>
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
