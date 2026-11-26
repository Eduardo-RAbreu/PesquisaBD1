package com.uel.bd1.controller;

import com.uel.bd1.dao.UsuarioDao;
import com.uel.bd1.model.LoginRequest;
import com.uel.bd1.model.Usuario;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UsuarioDao usuarioDao;

    public LoginController(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @PostMapping("/login")
    public ResponseEntity<?> entrar(
            @RequestBody LoginRequest dados,
            HttpServletRequest requisicao) throws SQLException {

        Usuario usuario = usuarioDao.autenticar(
            dados.email(),
            dados.senha()
        );

        if (usuario == null) {
            return ResponseEntity.status(401)
                .body("E-mail ou senha inválidos.");
        }

        HttpSession anterior = requisicao.getSession(false);

        if (anterior != null) {
            anterior.invalidate();
        }

        HttpSession sessao = requisicao.getSession(true);
        sessao.setAttribute("idUsuario", usuario.idUsu());

        return ResponseEntity.ok(usuario);
    }

    @GetMapping({"/login", "/me"})
    public ResponseEntity<Usuario> consultarLogin(
            HttpServletRequest requisicao) throws SQLException {

        HttpSession sessao = requisicao.getSession(false);

        if (sessao == null) {
            return ResponseEntity.status(401).build();
        }

        Integer idUsuario =
            (Integer) sessao.getAttribute("idUsuario");

        if (idUsuario == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioDao.listarID(idUsuario);

        if (usuario == null) {
            sessao.invalidate();
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> sair(
            HttpServletRequest requisicao) {

        HttpSession sessao = requisicao.getSession(false);

        if (sessao != null) {
            sessao.invalidate();
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarDadosInvalidos(
            IllegalArgumentException e) {

        return ResponseEntity.badRequest()
            .body(e.getMessage());
    }
}
