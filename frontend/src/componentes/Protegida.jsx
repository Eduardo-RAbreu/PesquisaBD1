import { Navigate } from 'react-router-dom'
import { useAuth } from '../AuthContext.jsx'

// Só deixa abrir a página quem está logado e os outros vão para o login.
export default function Protegida({ children }) {
  const { usuario, carregando } = useAuth()
  if (carregando) return <p className="vazio">Carregando...</p>
  if (!usuario) return <Navigate to="/login" replace />
  return children
}
