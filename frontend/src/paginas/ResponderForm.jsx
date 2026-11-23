import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'
import SeloTipo from '../componentes/SeloTipo.jsx'

const NOTAS = [1, 2, 3, 4, 5]

export default function ResponderForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { dados: pesquisa, erro: erroCarga, carregando } = useDados(`/api/responder/pesquisas/${id}`)

  // respostas: { [idQuestao]: nota (número) ou texto (string) }
  const [respostas, setRespostas] = useState({})
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  function responder(idQuestao, valor) {
    setRespostas((atuais) => ({ ...atuais, [idQuestao]: valor }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setErro('')

    const lista = pesquisa.questoes.map((q) => {
      const valor = respostas[q.idQuestao]
      return q.ehTexto
        ? { idQuestao: q.idQuestao, texto: (valor ?? '').toString() }
        : { idQuestao: q.idQuestao, valorEscala: valor ?? null }
    })
    const faltando = pesquisa.questoes.find((q, i) =>
      q.ehTexto ? lista[i].texto.trim() === '' : lista[i].valorEscala === null,
    )
    if (faltando) {
      setErro(`Responda a questão ${faltando.ordem} antes de enviar.`)
      return
    }

    setEnviando(true)
    try {
      const resultado = await api.post(`/api/responder/pesquisas/${id}`, { respostas: lista })
      navigate('/responder', { state: { mensagem: resultado.mensagem } })
    } catch (e) {
      setErro(e.message)
      setEnviando(false)
    }
  }

  if (carregando) return <p className="vazio">Carregando...</p>

  if (erroCarga || !pesquisa) {
    return (
      <>
        <h1>Responder pesquisa</h1>
        <Aviso tipo="erro">{erroCarga || 'Não foi possível abrir a pesquisa.'}</Aviso>
        <p><Link to="/responder">Voltar para as pesquisas</Link></p>
      </>
    )
  }

  return (
    <>
      <h1>{pesquisa.titulo}</h1>
      {pesquisa.descricao && <p className="subtitulo">{pesquisa.descricao}</p>}

      {pesquisa.anonima && (
        <p className="aviso aviso-info">
          Esta pesquisa é <strong>anônima</strong>: o sistema registra que você respondeu, mas não
          liga o seu nome ao que você respondeu.
        </p>
      )}
      <Aviso tipo="erro">{erro}</Aviso>

      <form className="folha-resposta" onSubmit={enviar}>
        {pesquisa.questoes.map((q) => (
          <fieldset className="questao-resp" key={q.idQuestao}>
            <legend>{q.ordem}. {q.descricao}</legend>
            <div className="questao-meta">
              <span>{q.nomeTema}</span>
              <SeloTipo nome={q.nomeTipo} />
            </div>

            {q.ehTexto ? (
              <textarea
                rows={3}
                aria-label={`Resposta da questão ${q.ordem}`}
                value={respostas[q.idQuestao] ?? ''}
                onChange={(e) => responder(q.idQuestao, e.target.value)}
              />
            ) : (
              <>
                <div className="escala" role="radiogroup" aria-label={`Nota da questão ${q.ordem}`}>
                  {NOTAS.map((n) => (
                    <label className="nota" key={n}>
                      <input
                        type="radio"
                        name={`questao-${q.idQuestao}`}
                        value={n}
                        checked={respostas[q.idQuestao] === n}
                        onChange={() => responder(q.idQuestao, n)}
                      />
                      <span>{n}</span>
                    </label>
                  ))}
                </div>
                <div className="escala-legenda">
                  <span>1 = muito insatisfeito</span>
                  <span>5 = muito satisfeito</span>
                </div>
              </>
            )}
          </fieldset>
        ))}

        <div className="botoes">
          <button type="submit" className="botao" disabled={enviando}>
            {enviando ? 'Enviando...' : 'Enviar respostas'}
          </button>
          <Link to="/responder">Cancelar</Link>
        </div>
      </form>
    </>
  )
}
