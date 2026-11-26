import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api, formatarData } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

export default function Pesquisas() {
  const location = useLocation()
  const { dados: pesquisas, erro: erroCarga, carregando, recarregar } = useDados('/api/pesquisas')
  const [mensagem, setMensagem] = useState(location.state?.mensagem ?? '')
  const [erro, setErro] = useState('')

  async function excluir(pesquisa) {
    if (!window.confirm(`Excluir a pesquisa "${pesquisa.titulo}"?`)) return
    setErro('')
    setMensagem('')
    try {
      await api.delete(`/api/pesquisas/${pesquisa.id}`)
      setMensagem('Pesquisa excluída.')
      recarregar()
    } catch (e) {
      setErro(e.message)
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <div>
          <h1>Pesquisas</h1>
          <p className="subtitulo">Cada pesquisa reúne questões, uma data, um horário e os grupos que devem responder.</p>
        </div>
        <Link className="botao" to="/pesquisas/nova">Nova pesquisa</Link>
      </div>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro || erroCarga}</Aviso>

      {carregando && !pesquisas && <p className="vazio">Carregando...</p>}
      {pesquisas && pesquisas.length === 0 && (
        <p className="vazio">Nenhuma pesquisa cadastrada. Use "Nova pesquisa" para montar a primeira.</p>
      )}

      {pesquisas && pesquisas.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Pesquisa</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Questões</th>
                <th>Grupos</th>
                <th>Respostas</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {pesquisas.map((p) => (
                <tr key={p.id}>
                  <td>
                    <strong>{p.titulo}</strong>
                    {p.descricao && <div className="detalhe">{p.descricao}</div>}
                  </td>
                  <td>{formatarData(p.dataPesquisa)}</td>
                  <td>{p.horaIni} às {p.horaFim}</td>
                  <td>{p.totalQuestoes}</td>
                  <td>{p.totalGrupos}</td>
                  <td>
                    <span className={`selo ${p.anonima ? 'selo-escala' : 'selo-texto'}`}>
                      {p.anonima ? 'Anônimas' : 'Identificadas'}
                    </span>
                  </td>
                  <td className="acoes">
                    <Link to={`/pesquisas/${p.id}/respostas`}>Ver respostas</Link>
                    <Link to={`/pesquisas/${p.id}/editar`}>Editar</Link>
                    <button type="button" className="link-perigo" onClick={() => excluir(p)}>
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
