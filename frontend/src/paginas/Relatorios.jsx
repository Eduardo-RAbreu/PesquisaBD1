import { useState } from 'react'
import MediaPorTema from './relatorios/MediaPorTema.jsx'
import MediaPorGrupo from './relatorios/MediaPorGrupo.jsx'
import Evolucao from './relatorios/Evolucao.jsx'
import RankingQuestoes from './relatorios/RankingQuestoes.jsx'
import Adesao from './relatorios/Adesao.jsx'

const ABAS = [
  { id: 'tema', titulo: 'Média por tema', Tela: MediaPorTema },
  { id: 'grupo', titulo: 'Comparação entre grupos', Tela: MediaPorGrupo },
  { id: 'evolucao', titulo: 'Evolução no tempo', Tela: Evolucao },
  { id: 'ranking', titulo: 'Ranking de questões', Tela: RankingQuestoes },
  { id: 'adesao', titulo: 'Quem respondeu', Tela: Adesao },
]

export default function Relatorios() {
  const [aba, setAba] = useState('tema')
  const { Tela } = ABAS.find((a) => a.id === aba)

  return (
    <>
      <h1>Relatórios</h1>
      <p className="subtitulo">
        Resultados das pesquisas de satisfação. As notas vão de 1 a 5, e só as questões de escala entram nas médias.
      </p>

      <div className="abas" role="tablist" aria-label="Relatórios">
        {ABAS.map((a) => (
          <button
            key={a.id}
            type="button"
            role="tab"
            aria-selected={a.id === aba}
            className={a.id === aba ? 'aba ativa' : 'aba'}
            onClick={() => setAba(a.id)}
          >
            {a.titulo}
          </button>
        ))}
      </div>

      <Tela />
    </>
  )
}
