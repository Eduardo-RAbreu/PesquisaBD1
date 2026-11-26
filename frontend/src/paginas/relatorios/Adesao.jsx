import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatarData } from '../../api.js'
import { useDados } from '../../useDados.js'
import Aviso from '../../componentes/Aviso.jsx'

export default function Adesao() {
  const [escolhida, setEscolhida] = useState('')
  const { dados: pesquisas } = useDados('/api/pesquisas')

  const idPesquisa = escolhida || (pesquisas && pesquisas.length > 0 ? String(pesquisas[0].id) : '')
  const { dados, erro, carregando } = useDados(
    idPesquisa ? `/api/relatorios/adesao?idPesquisa=${idPesquisa}` : null,
  )

  return (
    <section>
      <p className="descricao-relatorio">
        Mostra, por grupo, quantas pessoas deviam responder e quantas já responderam. A tabela lista quem ainda não respondeu.
      </p>

      <div className="filtros">
        <div>
          <label htmlFor="pesqAdesao">Pesquisa</label>
          <select id="pesqAdesao" value={idPesquisa} onChange={(e) => setEscolhida(e.target.value)}>
            {(pesquisas ?? []).map((p) => (
              <option key={p.id} value={p.id}>{p.titulo} ({formatarData(p.dataPesquisa)})</option>
            ))}
          </select>
        </div>
      </div>

      <Aviso tipo="erro">{erro}</Aviso>
      {carregando && <p className="vazio">Carregando...</p>}
      {pesquisas && pesquisas.length === 0 && <p className="vazio">Ainda não há pesquisas cadastradas.</p>}
      {dados && dados.grupos.length === 0 && (
        <p className="vazio">Esta pesquisa ainda não foi indicada para nenhum grupo.</p>
      )}

      {dados && dados.grupos.length > 0 && (
        <>
          <div className="grafico">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dados.grupos} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="nomeGrupo" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Legend />
                <Bar dataKey="esperados" name="Deviam responder" fill="#7F8F9C" radius={[4, 4, 0, 0]} />
                <Bar dataKey="respondidos" name="Já responderam" fill="#1B4E77" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <h2 className="secao-relatorio">
            Ainda não responderam ({dados.pendentes.length})
          </h2>
          {dados.pendentes.length === 0 ? (
            <p className="vazio">Todos já responderam.</p>
          ) : (
            <div className="tabela-rolagem">
              <table>
                <thead>
                  <tr><th>Nome</th><th>E-mail</th></tr>
                </thead>
                <tbody>
                  {dados.pendentes.map((u) => (
                    <tr key={u.id}>
                      <td>{u.nome}</td>
                      <td>{u.email}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </section>
  )
}
