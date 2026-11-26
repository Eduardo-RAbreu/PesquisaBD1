import { Link, useLocation } from 'react-router-dom'
import { formatarData } from '../api.js'
import { useAuth } from '../AuthContext.jsx'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

const ROTULO = { ABERTA: 'Aberta', FUTURA: 'Ainda não começou', ENCERRADA: 'Encerrada' }

export default function Responder() {
  const { usuario } = useAuth()
  const location = useLocation()
  const { dados: pesquisas, erro, carregando } = useDados('/api/responder/pesquisas')
  const mensagem = location.state?.mensagem ?? ''

  return (
    <>
      <h1>Responder pesquisas</h1>
      <p className="subtitulo">
        Pesquisas indicadas para <strong>{usuario?.nome}</strong>.
      </p>

      <Aviso tipo="sucesso">{mensagem}</Aviso>
      <Aviso tipo="erro">{erro}</Aviso>

      {carregando && <p className="vazio">Carregando...</p>}
      {pesquisas && pesquisas.length === 0 && (
        <p className="vazio">Nenhuma pesquisa foi indicada para os seus grupos.</p>
      )}

      {pesquisas && pesquisas.length > 0 && (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Pesquisa</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Situação</th>
                <th>Ação</th>
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
                  <td>
                    {p.jaRespondeu ? (
                      <span className="selo selo-ok">Respondida</span>
                    ) : (
                      <span className={`selo ${p.situacao === 'ABERTA' ? 'selo-escala' : 'selo-neutro'}`}>
                        {ROTULO[p.situacao]}
                      </span>
                    )}
                  </td>
                  <td className="acoes">
                    {!p.jaRespondeu && p.situacao === 'ABERTA' && <Link to={`/responder/${p.id}`}>Responder</Link>}
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
