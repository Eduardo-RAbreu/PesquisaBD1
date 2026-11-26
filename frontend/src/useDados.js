import { useEffect, useState } from 'react'
import { api } from './api.js'

export function useDados(url) {
  const [versao, setVersao] = useState(0)
  const [estado, setEstado] = useState({ chave: null, dados: null, erro: '' })
  const chave = url ? `${url}#${versao}` : null

  useEffect(() => {
    if (!chave) return undefined
    let cancelado = false
    api.get(url)
      .then((dados) => { if (!cancelado) setEstado({ chave, dados, erro: '' }) })
      .catch((e) => { if (!cancelado) setEstado({ chave, dados: null, erro: e.message }) })
    return () => { cancelado = true }
  }, [chave, url])

  const atual = chave !== null && estado.chave === chave
  return {
    dados: url ? estado.dados : null,
    erro: atual ? estado.erro : '',
    carregando: chave !== null && !atual,
    recarregar: () => setVersao((v) => v + 1),
  }
}
