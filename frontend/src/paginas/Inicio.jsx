import { Link } from 'react-router-dom'

const PASSOS = [
  { para: '/temas', titulo: 'Temas', texto: 'Os assuntos que agrupam as perguntas, como atendimento ou infraestrutura.' },
  { para: '/questoes', titulo: 'Questões', texto: 'Perguntas em escala de 1 a 5 ou de texto livre, cada uma ligada a um tema.' },
  { para: '/grupos', titulo: 'Grupos', texto: 'Turmas ou categorias de pessoas que vão responder as pesquisas.' },
  { para: '/usuarios', titulo: 'Usuários', texto: 'As pessoas cadastradas e os grupos a que pertencem.' },
  { para: '/pesquisas', titulo: 'Pesquisas', texto: 'Escolha as questões, a data, o horário e os grupos que devem responder.' },
  { para: '/responder', titulo: 'Responder', texto: 'Entre com o seu e-mail e responda as pesquisas indicadas para você.' },
  { para: '/relatorios', titulo: 'Relatórios', texto: 'Tabelas e gráficos que comparam temas, grupos e períodos.' },
]

export default function Inicio() {
  return (
    <>
      <section className="hero">
        <div className="hero-texto">
          <h1>Pesquisas de satisfação</h1>
          <p className="subtitulo">
            Cadastre as perguntas, monte pesquisas, aplique aos grupos e acompanhe os resultados.
          </p>
          <div className="hero-acoes">
            <Link className="botao botao-coral" to="/responder">Responder uma pesquisa</Link>
            <Link className="botao-contorno" to="/relatorios">Ver relatórios</Link>
          </div>
        </div>

        {/* enfeite: a escala de 1 a 5 que as pesquisas usam */}
        <div className="hero-escala" aria-hidden="true">
          <span>1</span>
          <span>2</span>
          <span>3</span>
          <span className="cheia">4</span>
          <span className="cheia">5</span>
        </div>
      </section>

      <div className="cartoes">
        {PASSOS.map((p, i) => (
          <Link className="cartao" to={p.para} key={p.para}>
            <span className="cartao-passo">{i + 1}</span>
            <h2>{p.titulo}</h2>
            <p>{p.texto}</p>
          </Link>
        ))}
      </div>
    </>
  )
}
