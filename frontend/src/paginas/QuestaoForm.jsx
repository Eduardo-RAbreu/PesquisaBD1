import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import Aviso from '../componentes/Aviso.jsx'

// cadastrar e editar questões
export default function QuestaoForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()

  const [temas, setTemas] = useState([])
  const [tipos, setTipos] = useState([])
  const [descricao, setDescricao] = useState('')
  const [idTema, setIdTema] = useState('')
  const [idTipoQuestao, setIdTipoQuestao] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    async function carregar() {
      try {
        const [listaTemas, listaTipos] = await Promise.all([
          api.get('/api/temas'),
          api.get('/api/tipos-questao'),
        ])
        setTemas(listaTemas)
        setTipos(listaTipos)

        if (editando) {
          const q = await api.get(`/api/questoes/${id}`)
          setDescricao(q.descricao)
          setIdTema(String(q.idTema))
          setIdTipoQuestao(String(q.idTipoQuestao))
        }
      } catch (e) {
        setErro(e.message)
      } finally {
        setCarregando(false)
      }
    }
    carregar()
  }, [id, editando])

  async function salvar(evento) {
    evento.preventDefault()
    setErro('')
    const corpo = {
      descricao,
      idTema: idTema ? Number(idTema) : null,
      idTipoQuestao: idTipoQuestao ? Number(idTipoQuestao) : null,
    }
    try {
      if (editando) {
        await api.put(`/api/questoes/${id}`, corpo)
      } else {
        await api.post('/api/questoes', corpo)
      }
      navigate('/questoes', {
        state: { mensagem: editando ? 'Questão atualizada.' : 'Questão cadastrada.' },
      })
    } catch (e) {
      setErro(e.message)
    }
  }

  if (carregando) return <p className="vazio">Carregando...</p>

  return (
    <>
      <h1>{editando ? 'Editar questão' : 'Nova questão'}</h1>
      <Aviso tipo="erro">{erro}</Aviso>

      <form className="formulario" onSubmit={salvar}>
        <label htmlFor="descricao">Enunciado</label>
        <textarea
          id="descricao"
          rows={4}
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          required
        />

        <label htmlFor="idTema">Tema</label>
        <select id="idTema" value={idTema} onChange={(e) => setIdTema(e.target.value)} required>
          <option value="">Escolha um tema</option>
          {temas.map((t) => (
            <option key={t.id} value={t.id}>{t.nomeTema}</option>
          ))}
        </select>
        {temas.length === 0 && (
          <p className="dica">Ainda não há temas. <Link to="/temas">Cadastre um tema</Link> primeiro.</p>
        )}

        <label htmlFor="idTipoQuestao">Tipo de questão</label>
        <select
          id="idTipoQuestao"
          value={idTipoQuestao}
          onChange={(e) => setIdTipoQuestao(e.target.value)}
          required
        >
          <option value="">Escolha um tipo</option>
          {tipos.map((t) => (
            <option key={t.id} value={t.id}>{t.nomeTipo}</option>
          ))}
        </select>

        <div className="botoes">
          <button type="submit" className="botao">Salvar questão</button>
          <Link to="/questoes">Cancelar</Link>
        </div>
      </form>
    </>
  )
}
