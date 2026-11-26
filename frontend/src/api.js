async function requisicao(metodo, url, corpo) {
  const opcoes = { method: metodo, headers: {}, credentials: 'same-origin' }
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

  const texto = await resposta.text()
  let dados = null
  if (texto) {
    try { dados = JSON.parse(texto) } catch { dados = texto }
  }
  if (!resposta.ok) {
    if (resposta.status === 401 && url !== '/api/login' && url !== '/api/me') {
      window.dispatchEvent(new Event('sessao-expirada'))
    }
    throw new Error((typeof dados === 'string' ? dados : dados?.erro)
      || (resposta.status === 401 ? 'Faça login para continuar.' : 'Não foi possível concluir a operação.'))
  }
  return dados
}

export const api = {
  get: (url) => requisicao('GET', url),
  post: (url, corpo) => requisicao('POST', url, corpo),
  put: (url, corpo) => requisicao('PUT', url, corpo),
  delete: (url) => requisicao('DELETE', url),
}

export function formatarData(iso) {
  if (!iso) return ''
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

export function formatarNota(valor) {
  if (valor === null || valor === undefined) return '—'
  return Number(valor).toFixed(2).replace('.', ',')
}
