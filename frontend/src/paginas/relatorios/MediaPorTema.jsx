import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatarNota } from '../../api.js'
import { useDados } from '../../useDados.js'
import Aviso from '../../componentes/Aviso.jsx'

// Relatório 1: média de satisfação por tema, com filtro de período.
export default function MediaPorTema() {
  const [de, setDe] = useState('')
  const [ate, setAte] = useState('')
  const [minRespostas, setMinRespostas] = useState(1)
  // os filtros só valem depois de apertar "Filtrar"
  const [filtros, setFiltros] = useState({ de: '', ate: '', minRespostas: 1 })

  const params = new URLSearchParams({ minRespostas: String(filtros.minRespostas) })
  if (filtros.de) params.set('de', filtros.de)
  if (filtros.ate) params.set('ate', filtros.ate)
  const { dados, erro, carregando } = useDados(`/api/relatorios/media-por-tema?${params}`)

  function aplicar(evento) {
    evento.preventDefault()
    setFiltros({ de, ate, minRespostas: Number(minRespostas) || 1 })
  }

  return (
    <section>
      <p className="descricao-relatorio">
        Compara a média das notas de cada tema. Use o período para ver só as pesquisas de certas datas.
      </p>

      <form className="filtros" onSubmit={aplicar}>
        <div>
          <label htmlFor="de">De</label>
          <input id="de" type="date" value={de} onChange={(e) => setDe(e.target.value)} />
        </div>
        <div>
          <label htmlFor="ate">Até</label>
          <input id="ate" type="date" value={ate} onChange={(e) => setAte(e.target.value)} />
        </div>
        <div>
          <label htmlFor="min">Mínimo de respostas</label>
          <input id="min" type="number" min="1" value={minRespostas} onChange={(e) => setMinRespostas(e.target.value)} />
        </div>
        <button type="submit" className="botao">Filtrar</button>
      </form>

      <Aviso tipo="erro">{erro}</Aviso>
      {carregando && <p className="vazio">Carregando...</p>}
      {dados && dados.length === 0 && <p className="vazio">Nenhum resultado para esses filtros.</p>}

      {dados && dados.length > 0 && (
        <>
          <div className="grafico">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dados} margin={{ top: 10, right: 20, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="nomeTema" />
                <YAxis domain={[0, 5]} ticks={[0, 1, 2, 3, 4, 5]} />
                <Tooltip formatter={(valor) => formatarNota(valor)} />
                <Bar dataKey="media" name="Média" fill="#1B4E77" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="tabela-rolagem">
            <table>
              <thead>
                <tr><th>Tema</th><th>Média</th><th>Respostas</th></tr>
              </thead>
              <tbody>
                {dados.map((t) => (
                  <tr key={t.idTema}>
                    <td>{t.nomeTema}</td>
                    <td>{formatarNota(t.media)}</td>
                    <td>{t.totalRespostas}</td>
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
