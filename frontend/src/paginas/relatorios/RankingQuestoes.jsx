import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatarNota } from '../../api.js'
import { useDados } from '../../useDados.js'
import Aviso from '../../componentes/Aviso.jsx'

const FILTROS_INICIAIS = { busca: '', idsTemas: [], ordem: 'melhores', minRespostas: 1, limite: 10 }

function cortar(texto, max) {
  return texto.length > max ? `${texto.slice(0, max - 1)}…` : texto
}

// Relatório 4: ranking das questões mais bem (ou pior) avaliadas.
export default function RankingQuestoes() {
  const [form, setForm] = useState(FILTROS_INICIAIS)
  const [filtros, setFiltros] = useState(FILTROS_INICIAIS)
  const { dados: temas } = useDados('/api/temas')

  const params = new URLSearchParams({
    ordem: filtros.ordem,
    minRespostas: String(filtros.minRespostas),
    limite: String(filtros.limite),
  })
  if (filtros.busca.trim()) params.set('busca', filtros.busca.trim())
  if (filtros.idsTemas.length > 0) params.set('idsTemas', filtros.idsTemas.join(','))
  const { dados, erro, carregando } = useDados(`/api/relatorios/ranking-questoes?${params}`)

  function mudar(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }))
  }

  function alternarTema(idTema) {
    setForm((atual) => ({
      ...atual,
      idsTemas: atual.idsTemas.includes(idTema)
        ? atual.idsTemas.filter((x) => x !== idTema)
        : [...atual.idsTemas, idTema],
    }))
  }

  function aplicar(evento) {
    evento.preventDefault()
    setFiltros({
      ...form,
      minRespostas: Number(form.minRespostas) || 1,
      limite: Math.min(50, Math.max(1, Number(form.limite) || 10)),
    })
  }

  const barras = (dados ?? []).map((q) => ({ ...q, rotulo: `${q.posicao}º ${cortar(q.descricao, 34)}` }))

  return (
    <section>
      <p className="descricao-relatorio">
        Lista as questões de escala com as melhores ou as piores médias. Dá para buscar por uma palavra do enunciado e escolher os temas.
      </p>

      <form className="filtros" onSubmit={aplicar}>
        <div>
          <label htmlFor="ordem">Mostrar</label>
          <select id="ordem" value={form.ordem} onChange={(e) => mudar('ordem', e.target.value)}>
            <option value="melhores">Melhores avaliadas</option>
            <option value="piores">Piores avaliadas</option>
          </select>
        </div>
        <div>
          <label htmlFor="busca">Palavra no enunciado</label>
          <input id="busca" type="text" value={form.busca} onChange={(e) => mudar('busca', e.target.value)} />
        </div>
        <div>
          <label htmlFor="limite">Quantas questões</label>
          <input id="limite" type="number" min="1" max="50" value={form.limite} onChange={(e) => mudar('limite', e.target.value)} />
        </div>
        <div>
          <label htmlFor="minRank">Mínimo de respostas</label>
          <input id="minRank" type="number" min="1" value={form.minRespostas} onChange={(e) => mudar('minRespostas', e.target.value)} />
        </div>
        <button type="submit" className="botao">Filtrar</button>
      </form>

      <div className="filtro-temas">
        <span>Temas:</span>
        {(temas ?? []).map((t) => (
          <label className="mini-opcao" key={t.id}>
            <input type="checkbox" checked={form.idsTemas.includes(t.id)} onChange={() => alternarTema(t.id)} />
            {t.nomeTema}
          </label>
        ))}
        <small>(sem nenhum marcado, vale para todos)</small>
      </div>

      <Aviso tipo="erro">{erro}</Aviso>
      {carregando && <p className="vazio">Carregando...</p>}
      {dados && dados.length === 0 && <p className="vazio">Nenhuma questão encontrada para esses filtros.</p>}

      {dados && dados.length > 0 && (
        <>
          <div className="grafico">
            <ResponsiveContainer width="100%" height={Math.max(220, barras.length * 38 + 50)}>
              <BarChart data={barras} layout="vertical" margin={{ top: 5, right: 30, left: 10, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" domain={[0, 5]} ticks={[0, 1, 2, 3, 4, 5]} />
                <YAxis type="category" dataKey="rotulo" width={270} tick={{ fontSize: 12 }} />
                <Tooltip
                  formatter={(valor) => formatarNota(valor)}
                  labelFormatter={(rotulo, itens) => itens?.[0]?.payload?.descricao ?? rotulo}
                />
                <Bar dataKey="media" name="Média" fill="#0e6a70" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="tabela-rolagem">
            <table>
              <thead>
                <tr><th>Posição</th><th>Questão</th><th>Tema</th><th>Média</th><th>Respostas</th></tr>
              </thead>
              <tbody>
                {dados.map((q) => (
                  <tr key={q.idQuestao}>
                    <td>{q.posicao}º</td>
                    <td>{q.descricao}</td>
                    <td>{q.nomeTema}</td>
                    <td>{formatarNota(q.media)}</td>
                    <td>{q.totalRespostas}</td>
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
