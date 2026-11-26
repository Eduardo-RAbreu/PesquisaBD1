package com.uel.bd1.config;

import com.uel.bd1.dao.UsuarioDao;
import com.uel.bd1.model.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.sql.SQLException;

@Component
public class AutorizacaoFilter extends OncePerRequestFilter {
    private final UsuarioDao usuarioDao;

    public AutorizacaoFilter(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
            FilterChain corrente) throws ServletException, IOException {
        String caminho = requisicao.getServletPath();
        String metodo = requisicao.getMethod();
        if (!(caminho.equals("/api") || caminho.startsWith("/api/"))
                || (caminho.equals("/api/login") && metodo.equals("POST"))) {
            corrente.doFilter(requisicao, resposta);
            return;
        }

        HttpSession sessao = requisicao.getSession(false);
        if (sessao == null || !(sessao.getAttribute("idUsuario") instanceof Integer id) || id <= 0) {
            negar(resposta, 401, "Faça login para acessar o sistema.");
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioDao.listarID(id);
        } catch (SQLException e) {
            negar(resposta, 503, "Não foi possível verificar a sessão. Tente novamente.");
            return;
        }
        if (usuario == null) {
            sessao.invalidate();
            negar(resposta, 401, "Sua sessão não é mais válida. Faça login novamente.");
            return;
        }

        requisicao.setAttribute("usuarioAutenticado", usuario);
        corrente.doFilter(requisicao, resposta);
    }

    private void negar(HttpServletResponse resposta, int status, String mensagem) throws IOException {
        resposta.setStatus(status);
        resposta.setContentType("application/json");
        resposta.setCharacterEncoding("UTF-8");
        resposta.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
