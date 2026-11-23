import { createContext, useContext, useEffect, useState } from 'react'
import { api } from './api.js'

// Guarda quem está logado. O back-end mantém a sessão 
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [carregando, setCarregando] = useState(true)

  // verifica se alguem esta logado
  useEffect(() => {
    api.get('/api/me')
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCarregando(false))
  }, [])

  async function entrar(email) {
    const logado = await api.post('/api/login', { email })
    setUsuario(logado)
    return logado
  }

  async function sair() {
    await api.post('/api/logout')
    setUsuario(null)
  }

  return (
    <AuthContext.Provider value={{ usuario, carregando, entrar, sair }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
