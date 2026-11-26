package com.uel.bd1.controller;

import com.uel.bd1.dao.ConsultaDao;
import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.DadosTela.*;
import com.uel.bd1.model.EnvioRespostasRequest;
import com.uel.bd1.service.ParticipacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/responder/pesquisas")
public class ResponderController {
    private final ConsultaDao consulta;
    private final ParticipacaoService service;

    public ResponderController(ConsultaDao consulta, ParticipacaoService service) {
        this.consulta = consulta;
        this.service = service;
    }

    private int usuario(HttpServletRequest requisicao) {
        HttpSession sessao = requisicao.getSession(false);
        if (sessao == null || !(sessao.getAttribute("idUsuario") instanceof Integer id) || id <= 0) {
            throw new ApiException(401, "Faça login para responder.");
        }
        return id;
    }

    @GetMapping
    public List<PesquisaDisponivel> listar(HttpServletRequest requisicao) throws SQLException {
        return consulta.listarDisponiveis(usuario(requisicao));
    }

    @GetMapping("/{id}")
    public FormularioPesquisa abrir(@PathVariable("id") int id, HttpServletRequest requisicao) throws SQLException {
        return consulta.formulario(usuario(requisicao), id);
    }

    @PostMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Map<String, String> enviar(@PathVariable("id") int id,
            @RequestBody EnvioRespostasRequest dados, HttpServletRequest requisicao) throws SQLException {
        service.enviar(usuario(requisicao), id, dados);
        return Map.of("mensagem", "Respostas enviadas com sucesso.");
    }
}
