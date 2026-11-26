import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatarData, formatarNota } from '../../api.js'
import { useDados } from '../../useDados.js'
import Aviso from '../../componentes/Aviso.jsx'

export default function MediaPorGrupo() {
  const [idPesquisa, setIdPesquisa] = useState('')
  const { dados: pesquisas } = useDados('/api/pesquisas')
  const url = '/api/relatorios/media-por-grupo' + (idPesquisa ? `?idPesquisa=${idPesquisa}` : '')
  const { dados, erro, carregando } = useDados(url)

  return (
    <section>
      <p className="descricao-relatorio">
        Compara a média de cada grupo de respondentes. Grupos que ainda não responderam aparecem na tabela, sem nota.
      </p>

      <div className="filtros">
        <div>
          <label htmlFor="pesq">Pesquisa</label>
          <select id="pesq" value={idPesquisa} onChange={(e) => setIdPesquisa(e.target.value)}>
            <option value="">Todas as pesquisas</option>
            {(pesquisas ?? []).map((p) => (
              <option key={p.id} value={p.id}>{p.titulo} ({formatarData(p.dataPesquisa)})</option>
            ))}
          </select>
        </div>
      </div>

      <Aviso tipo="erro">{erro}</Aviso>
      {carregando && <p className="vazio">Carregando...</p>}

      {dados && (
        <>
          <div className="grafico">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dados} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="nomeGrupo" />
                <YAxis domain={[0, 5]} ticks={[0, 1, 2, 3, 4, 5]} />
                <Tooltip formatter={(valor) => formatarNota(valor)} />
                <Bar dataKey="media" name="Média" fill="#1B4E77" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="tabela-rolagem">
            <table>
              <thead>
                <tr><th>Grupo</th><th>Média</th><th>Respostas</th></tr>
              </thead>
              <tbody>
                {dados.map((g) => (
                  <tr key={g.idGrupo}>
                    <td>{g.nomeGrupo}</td>
                    <td>{g.totalRespostas === 0 ? 'sem respostas' : formatarNota(g.media)}</td>
                    <td>{g.totalRespostas}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </section>
  )
}
