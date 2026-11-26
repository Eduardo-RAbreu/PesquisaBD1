import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { formatarData } from '../api.js'
import { useDados } from '../useDados.js'
import Aviso from '../componentes/Aviso.jsx'

export default function PesquisaRespostas() {
  const { id } = useParams()
  if (!/^\d+$/.test(id) || !Number.isSafeInteger(Number(id)) || Number(id) <= 0) {
    return <><Link to="/pesquisas">Voltar às pesquisas</Link><Aviso tipo="erro">Pesquisa inválida.</Aviso></>
  }
  return <Respostas key={id} id={Number(id)} />
}

function Respostas({ id }) {
  const pesquisaConsulta = useDados(`/api/pesquisas/${id}`)
  const participacoesConsulta = useDados(`/api/pesquisas/${id}/participacoes`)
  const respostasConsulta = useDados('/api/respostas')
  const questoesConsulta = useDados('/api/questoes')
  const gruposConsulta = useDados('/api/grupos')
  const [idQuestao, setIdQuestao] = useState('')
  const [idRespondente, setIdRespondente] = useState('')
  const consultas = [pesquisaConsulta, participacoesConsulta, respostasConsulta, questoesConsulta, gruposConsulta]
  const carregando = consultas.some((consulta) => consulta.carregando)
  const erro = consultas.find((consulta) => consulta.erro)?.erro

  function atualizar() {
    consultas.forEach((consulta) => consulta.recarregar())
  }

  if (carregando || erro) {
    return <>
      <Link to="/pesquisas">Voltar às pesquisas</Link>
      <h1>Respostas da pesquisa</h1>
      {carregando ? <p className="vazio" role="status">Carregando respostas...</p> : <>
        <Aviso tipo="erro">{erro}</Aviso>
        <button type="button" className="botao" onClick={atualizar}>Tentar novamente</button>
      </>}
    </>
  }

  const pesquisa = pesquisaConsulta.dados
  const participacoes = new Map(participacoesConsulta.dados
    .filter((participacao) => participacao.idPesquisa === id)
    .map((participacao) => [participacao.idPart, participacao]))
  const respostasDaPesquisa = respostasConsulta.dados.filter((resposta) => participacoes.has(resposta.idPart))
  const respostas = idRespondente && !pesquisa.anonima
    ? respostasDaPesquisa.filter((resposta) => resposta.idPart === Number(idRespondente))
    : respostasDaPesquisa
  const questoes = new Map(questoesConsulta.dados.map((questao) => [questao.id, questao]))
  const grupos = new Map(gruposConsulta.dados.map((grupo) => [grupo.id, grupo.nomeGrupo]))
  const porQuestao = new Map()
  for (const resposta of respostas) {
    if (!porQuestao.has(resposta.idQuestao)) porQuestao.set(resposta.idQuestao, [])
    porQuestao.get(resposta.idQuestao).push(resposta)
  }
  const idsQuestoes = [...new Set([...pesquisa.idsQuestoes, ...porQuestao.keys()])]
  const visiveis = idQuestao ? idsQuestoes.filter((valor) => valor === Number(idQuestao)) : idsQuestoes

  return <>
    <Link to="/pesquisas">Voltar às pesquisas</Link>
    <div className="cabecalho-pagina">
      <div>
        <h1>Respostas: {pesquisa.titulo}</h1>
        <p className="subtitulo">{formatarData(pesquisa.dataPesquisa)} · {pesquisa.anonima ? 'Pesquisa anônima' : 'Pesquisa identificada'}</p>
      </div>
      <button type="button" className="botao-contorno" onClick={atualizar}>Atualizar respostas</button>
    </div>
    {pesquisa.descricao && <p>{pesquisa.descricao}</p>}
    <p className="resumo-respostas" role="status">
      <strong>{participacoes.size}</strong> {participacoes.size === 1 ? 'participação' : 'participações'} · <strong>{respostasDaPesquisa.length}</strong> {respostasDaPesquisa.length === 1 ? 'resposta registrada' : 'respostas registradas'}
    </p>
    <p className="dica">{pesquisa.anonima
      ? 'As respostas desta pesquisa são anônimas. Os respondentes não são identificados.'
      : 'Confira quem respondeu cada questão ou selecione uma pessoa para ver suas respostas. Participações antigas sem vínculo salvo aparecem como identificação não registrada.'}</p>

    {respostasDaPesquisa.length === 0 ? <p className="vazio">Esta pesquisa ainda não possui respostas registradas.</p> : <>
      <div className="filtros">
        {!pesquisa.anonima && <div>
          <label htmlFor="respondenteRespostas">Respondente</label>
          <select id="respondenteRespostas" value={idRespondente} onChange={(evento) => setIdRespondente(evento.target.value)}>
            <option value="">Todos os respondentes</option>
            {[...participacoes.values()].map((participacao, indice) => <option key={participacao.idPart} value={participacao.idPart}>
              {participacao.nomeUsuario ?? `Identificação não registrada (participação ${indice + 1})`}
            </option>)}
          </select>
        </div>}
        <div className="filtro-questao-respostas">
          <label htmlFor="questaoRespostas">Questão</label>
          <select id="questaoRespostas" value={idQuestao} onChange={(evento) => setIdQuestao(evento.target.value)}>
            <option value="">Todas as questões</option>
            {idsQuestoes.map((valor, indice) => <option key={valor} value={valor}>
              {indice + 1}. {questoes.get(valor)?.descricao ?? 'Questão indisponível'}
            </option>)}
          </select>
        </div>
      </div>
      <div className="lista-respostas-pesquisa">
        {visiveis.map((valor) => {
          const questao = questoes.get(valor)
          const itens = porQuestao.get(valor) ?? []
          return <section className="bloco-respostas" key={valor} aria-labelledby={`tituloQuestao${valor}`}>
            <h2 id={`tituloQuestao${valor}`}>{idsQuestoes.indexOf(valor) + 1}. {questao?.descricao ?? 'Questão indisponível'}</h2>
            <p className="detalhe">{[questao?.nomeTema, questao?.nomeTipo, `${itens.length} ${itens.length === 1 ? 'resposta' : 'respostas'}`].filter(Boolean).join(' · ')}</p>
            {itens.length === 0 ? <p className="vazio">Nenhuma resposta para esta questão.</p> : <div className="tabela-rolagem">
              <table>
                <thead><tr>{!pesquisa.anonima && <th scope="col">Respondente</th>}<th scope="col">Resposta</th><th scope="col">Grupo respondente</th></tr></thead>
                <tbody>{itens.map((resposta) => <tr key={resposta.idResposta}>
                  {!pesquisa.anonima && <td>{participacoes.get(resposta.idPart).nomeUsuario ?? 'Identificação não registrada'}</td>}
                  <td className="texto-resposta">{resposta.valorEscala != null ? `${resposta.valorEscala} / 5` : resposta.texto}</td>
                  <td>{grupos.get(participacoes.get(resposta.idPart).idGrupoRespondente) ?? 'Não informado'}</td>
                </tr>)}</tbody>
              </table>
            </div>}
          </section>
        })}
      </div>
    </>}
  </>
}
