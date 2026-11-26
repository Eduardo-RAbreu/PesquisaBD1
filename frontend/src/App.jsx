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
import PesquisaRespostas from './paginas/PesquisaRespostas.jsx'
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
        {usuario && <nav>
          <NavLink to="/responder">Responder</NavLink>
          <NavLink to="/pesquisas">Pesquisas</NavLink>
          <NavLink to="/questoes">Questões</NavLink>
          <NavLink to="/temas">Temas</NavLink>
          <NavLink to="/grupos">Grupos</NavLink>
          <NavLink to="/usuarios">Usuários</NavLink>
          <NavLink to="/relatorios">Relatórios</NavLink>
        </nav>}
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
          <Route path="/" element={<Protegida><Inicio /></Protegida>} />
          <Route path="/login" element={<Login />} />
          <Route path="/temas" element={<Protegida><Temas /></Protegida>} />
          <Route path="/questoes" element={<Protegida><Questoes /></Protegida>} />
          <Route path="/questoes/nova" element={<Protegida><QuestaoForm /></Protegida>} />
          <Route path="/questoes/:id/editar" element={<Protegida><QuestaoForm /></Protegida>} />
          <Route path="/grupos" element={<Protegida><Grupos /></Protegida>} />
          <Route path="/usuarios" element={<Protegida><Usuarios /></Protegida>} />
          <Route path="/pesquisas" element={<Protegida><Pesquisas /></Protegida>} />
          <Route path="/pesquisas/nova" element={<Protegida><PesquisaForm /></Protegida>} />
          <Route path="/pesquisas/:id/editar" element={<Protegida><PesquisaForm /></Protegida>} />
          <Route path="/pesquisas/:id/respostas" element={<Protegida><PesquisaRespostas /></Protegida>} />
          <Route path="/responder" element={<Protegida><Responder /></Protegida>} />
          <Route path="/responder/:id" element={<Protegida><ResponderForm /></Protegida>} />
          <Route path="/relatorios" element={<Protegida><Relatorios /></Protegida>} />
          <Route path="*" element={<Protegida><p className="vazio">Página não encontrada.</p></Protegida>} />
        </Routes>
      </main>
    </>
  )
}
