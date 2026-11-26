import { useState } from 'react'
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatarData, formatarNota } from '../../api.js'
import { useDados } from '../../useDados.js'
import Aviso from '../../componentes/Aviso.jsx'

// Relatório 3: evolução da média pesquisa a pesquisa, comparada com a média geral.
export default function Evolucao() {
  const [idTema, setIdTema] = useState('')
  const { dados: temas } = useDados('/api/temas')
  const url = '/api/relatorios/evolucao' + (idTema ? `?idTema=${idTema}` : '')
  const { dados, erro, carregando } = useDados(url)

  // o eixo X mostra só dia/mês
  const linhas = (dados ?? []).map((d) => ({ ...d, rotulo: formatarData(d.dataPesquisa).slice(0, 5) }))

  return (
    <section>
      <p className="descricao-relatorio">
        Mostra como a média mudou de uma pesquisa para outra, comparada com a média geral de todas as pesquisas.
      </p>

      <div className="filtros">
        <div>
          <label htmlFor="tema">Tema</label>
          <select id="tema" value={idTema} onChange={(e) => setIdTema(e.target.value)}>
            <option value="">Todos os temas</option>
            {(temas ?? []).map((t) => (
              <option key={t.id} value={t.id}>{t.nomeTema}</option>
            ))}
          </select>
        </div>
      </div>

      <Aviso tipo="erro">{erro}</Aviso>
      {carregando && <p className="vazio">Carregando...</p>}
      {dados && dados.length === 0 && <p className="vazio">Ainda não há respostas para este filtro.</p>}

      {dados && dados.length > 0 && (
        <>
          <div className="grafico">
            <ResponsiveContainer width="100%" height={320}>
              <LineChart data={linhas} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="rotulo" />
                <YAxis domain={[0, 5]} ticks={[0, 1, 2, 3, 4, 5]} />
                <Tooltip
                  formatter={(valor) => formatarNota(valor)}
                  labelFormatter={(rotulo, itens) => itens?.[0]?.payload?.titulo ?? rotulo}
                />
                <Legend />
                <Line type="monotone" dataKey="media" name="Média da pesquisa" stroke="#1B4E77" strokeWidth={2.5} dot={{ r: 4 }} />
                <Line type="monotone" dataKey="mediaGeral" name="Média geral" stroke="#C24A22" strokeWidth={2} strokeDasharray="6 4" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="tabela-rolagem">
            <table>
              <thead>
                <tr><th>Pesquisa</th><th>Data</th><th>Média</th><th>Média geral</th><th>Participações</th></tr>
              </thead>
              <tbody>
                {dados.map((p) => (
                  <tr key={p.idPesquisa}>
                    <td>{p.titulo}</td>
                    <td>{formatarData(p.dataPesquisa)}</td>
                    <td>{formatarNota(p.media)}</td>
                    <td>{formatarNota(p.mediaGeral)}</td>
                    <td>{p.participacoes}</td>
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
