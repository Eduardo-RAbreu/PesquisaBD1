async function requisicao(metodo, url, corpo) {
  const opcoes = { method: metodo, headers: {} }
  if (corpo !== undefined) {
    opcoes.headers['Content-Type'] = 'application/json'
    opcoes.body = JSON.stringify(corpo)
  }

  let resposta
  try {
    resposta = await fetch(url, opcoes)
  } catch {
    throw new Error('Não foi possível falar com o servidor. Confira se o back-end está rodando.')
  }

  if (resposta.status === 204) return null

  // O back-end devolve erros no formato {"erro": "mensagem"}
  const dados = await resposta.json().catch(() => null)
  if (!resposta.ok) {
    throw new Error(dados?.erro ?? 'O servidor não respondeu como esperado. Confira se o back-end está rodando.')
  }
  return dados
}

export const api = {
  get: (url) => requisicao('GET', url),
  post: (url, corpo) => requisicao('POST', url, corpo),
  put: (url, corpo) => requisicao('PUT', url, corpo),
  delete: (url) => requisicao('DELETE', url),
}

// "2026-11-27" -> "27/11/2026"
export function formatarData(iso) {
  if (!iso) return ''
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

// 3.5 -> "3,50"  (nulo vira "—")
export function formatarNota(valor) {
  if (valor === null || valor === undefined) return '—'
  return Number(valor).toFixed(2).replace('.', ',')
}
