import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api.js'
import Aviso from '../componentes/Aviso.jsx'
import SeloTipo from '../componentes/SeloTipo.jsx'

// cadastro e editar pesquisas
export default function PesquisaForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navigate = useNavigate()

  const [questoes, setQuestoes] = useState([])
  const [grupos, setGrupos] = useState([])
  const [titulo, setTitulo] = useState('')
  const [descricao, setDescricao] = useState('')
  const [dataPesquisa, setDataPesquisa] = useState('')
  const [horaIni, setHoraIni] = useState('')
  const [horaFim, setHoraFim] = useState('')
  const [anonima, setAnonima] = useState(true)
  const [idsQuestoes, setIdsQuestoes] = useState([])
  const [idsGrupos, setIdsGrupos] = useState([])
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    async function carregar() {
      try {
        const [listaQuestoes, listaGrupos] = await Promise.all([
          api.get('/api/questoes'),
          api.get('/api/grupos'),
        ])
        setQuestoes(listaQuestoes)
        setGrupos(listaGrupos)
        if (editando) {
          const p = await api.get(`/api/pesquisas/${id}`)
          setTitulo(p.titulo)
          setDescricao(p.descricao ?? '')
          setDataPesquisa(p.dataPesquisa)
          setHoraIni(p.horaIni)
          setHoraFim(p.horaFim)
          setAnonima(p.anonima)
          setIdsQuestoes(p.idsQuestoes)
          setIdsGrupos(p.idsGrupos)
        }
      } catch (e) {
        setErro(e.message)
      } finally {
        setCarregando(false)
      }
    }
    carregar()
  }, [id, editando])

  // marca ou desmarca um item de uma lista de IDs
  function alternar(setLista, idItem) {
    setLista((atuais) =>
      atuais.includes(idItem) ? atuais.filter((x) => x !== idItem) : [...atuais, idItem],
    )
  }

  async function salvar(evento) {
    evento.preventDefault()
    setErro('')
    // a ordem das questões na pesquisa segue a ordem da lista na tela
    const ordenadas = questoes.filter((q) => idsQuestoes.includes(q.id)).map((q) => q.id)
    const corpo = { titulo, descricao, dataPesquisa, horaIni, horaFim, anonima, idsQuestoes: ordenadas, idsGrupos }
    try {
      if (editando) {
        await api.put(`/api/pesquisas/${id}`, corpo)
      } else {
        await api.post('/api/pesquisas', corpo)
      }
      navigate('/pesquisas', {
        state: { mensagem: editando ? 'Pesquisa atualizada.' : 'Pesquisa cadastrada.' },
      })
    } catch (e) {
      setErro(e.message)
    }
  }

  if (carregando) return <p className="vazio">Carregando...</p>

  return (
    <>
      <h1>{editando ? 'Editar pesquisa' : 'Nova pesquisa'}</h1>
      <Aviso tipo="erro">{erro}</Aviso>

      <form className="formulario formulario-largo" onSubmit={salvar}>
        <h2 className="secao">Dados da pesquisa</h2>

        <label htmlFor="titulo">Título</label>
        <input
          id="titulo"
          type="text"
          maxLength={150}
          value={titulo}
          onChange={(e) => setTitulo(e.target.value)}
          required
        />

        <label htmlFor="descricao">Descrição <span className="opcional">(opcional)</span></label>
        <textarea
          id="descricao"
          rows={3}
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />

        <div className="grade-tres">
          <div>
            <label htmlFor="dataPesquisa">Data</label>
            <input
              id="dataPesquisa"
              type="date"
              value={dataPesquisa}
              onChange={(e) => setDataPesquisa(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="horaIni">Início</label>
            <input
              id="horaIni"
              type="time"
              value={horaIni}
              onChange={(e) => setHoraIni(e.target.value)}
              required
            />
          </div>
          <div>
            <label htmlFor="horaFim">Fim</label>
            <input
              id="horaFim"
              type="time"
              value={horaFim}
              onChange={(e) => setHoraFim(e.target.value)}
              required
            />
          </div>
        </div>

        <label className="opcao opcao-solta">
          <input type="checkbox" checked={anonima} onChange={(e) => setAnonima(e.target.checked)} />
          <span className="opcao-texto">
            <span>Respostas anônimas</span>
            <small>Quem respondeu não fica ligado ao que foi respondido.</small>
          </span>
        </label>

        <h2 className="secao">Quem deve responder</h2>
        {grupos.length === 0 && (
          <p className="dica">
            Ainda não há grupos. <Link to="/grupos">Cadastre um grupo</Link> primeiro.
          </p>
        )}
        <div className="grade-opcoes">
          {grupos.map((g) => (
            <label className="opcao" key={g.id}>
              <input
                type="checkbox"
                checked={idsGrupos.includes(g.id)}
                onChange={() => alternar(setIdsGrupos, g.id)}
              />
              <span className="opcao-texto">
                <span>{g.nomeGrupo}</span>
                <small>{g.totalUsuarios} {g.totalUsuarios === 1 ? 'usuário' : 'usuários'}</small>
              </span>
            </label>
          ))}
        </div>

        <h2 className="secao">Questões da pesquisa</h2>
        {questoes.length === 0 && (
          <p className="dica">
            Ainda não há questões. <Link to="/questoes/nova">Cadastre uma questão</Link> primeiro.
          </p>
        )}
        <div className="lista-opcoes">
          {questoes.map((q) => (
            <label className="opcao" key={q.id}>
              <input
                type="checkbox"
                checked={idsQuestoes.includes(q.id)}
                onChange={() => alternar(setIdsQuestoes, q.id)}
              />
              <span className="opcao-texto">
                <span>{q.descricao}</span>
                <small>
                  <span>{q.nomeTema}</span>
                  <SeloTipo nome={q.nomeTipo} />
                </small>
              </span>
            </label>
          ))}
        </div>

        <div className="botoes">
          <button type="submit" className="botao">Salvar pesquisa</button>
          <Link to="/pesquisas">Cancelar</Link>
        </div>
      </form>
    </>
  )
}
