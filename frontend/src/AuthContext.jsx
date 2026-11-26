import { createContext, useContext, useEffect, useState } from 'react'
import { api } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    api.get('/api/me')
      .then(setUsuario)
      .catch(() => setUsuario(null))
      .finally(() => setCarregando(false))
  }, [])

  useEffect(() => {
    const encerrar = () => setUsuario(null)
    window.addEventListener('sessao-expirada', encerrar)
    return () => window.removeEventListener('sessao-expirada', encerrar)
  }, [])

  async function entrar(email, senha) {
    const logado = await api.post('/api/login', { email: email.trim(), senha })
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
