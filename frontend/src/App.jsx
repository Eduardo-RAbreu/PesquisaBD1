import { Link, NavLink, Route, Routes, useNavigate } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'
import Protegida from './componentes/Protegida.jsx'
import Inicio from './paginas/Inicio.jsx'
import Login from './paginas/Login.jsx'
import Temas from './paginas/Temas.jsx'
import Questoes from './paginas/Questoes.jsx'
import QuestaoForm from './paginas/QuestaoForm.jsx'
import Grupos from './paginas/Grupos.jsx'
import Usuarios from './paginas/Usuarios.jsx'
import Pesquisas from './paginas/Pesquisas.jsx'
import PesquisaForm from './paginas/PesquisaForm.jsx'
import Responder from './paginas/Responder.jsx'
import ResponderForm from './paginas/ResponderForm.jsx'
import Relatorios from './paginas/Relatorios.jsx'

export default function App() {
  const { usuario, sair } = useAuth()
  const navigate = useNavigate()

  async function sairDoSistema() {
    await sair()
    navigate('/login')
  }

  return (
    <>
      <header className="topo">
        <Link className="marca" to="/">Pesquisas</Link>
        <nav>
          <NavLink to="/responder">Responder</NavLink>
          <NavLink to="/pesquisas">Pesquisas</NavLink>
          <NavLink to="/questoes">Questões</NavLink>
          <NavLink to="/temas">Temas</NavLink>
          <NavLink to="/grupos">Grupos</NavLink>
          <NavLink to="/usuarios">Usuários</NavLink>
          <NavLink to="/relatorios">Relatórios</NavLink>
        </nav>
        <div className="conta">
          {usuario ? (
            <>
              <span>{usuario.nome}</span>
              <button type="button" className="link-simples" onClick={sairDoSistema}>Sair</button>
            </>
          ) : (
            <Link to="/login">Entrar</Link>
          )}
        </div>
      </header>

      <main>
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/login" element={<Login />} />
          <Route path="/temas" element={<Temas />} />
          <Route path="/questoes" element={<Questoes />} />
          <Route path="/questoes/nova" element={<QuestaoForm />} />
          <Route path="/questoes/:id/editar" element={<QuestaoForm />} />
          <Route path="/grupos" element={<Grupos />} />
          <Route path="/usuarios" element={<Usuarios />} />
          <Route path="/pesquisas" element={<Pesquisas />} />
          <Route path="/pesquisas/nova" element={<PesquisaForm />} />
          <Route path="/pesquisas/:id/editar" element={<PesquisaForm />} />
          <Route path="/responder" element={<Protegida><Responder /></Protegida>} />
          <Route path="/responder/:id" element={<Protegida><ResponderForm /></Protegida>} />
          <Route path="/relatorios" element={<Relatorios />} />
          <Route path="*" element={<p className="vazio">Página não encontrada.</p>} />
        </Routes>
      </main>
    </>
  )
}
