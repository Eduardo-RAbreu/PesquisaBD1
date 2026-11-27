package com.uel.bd1.controller;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.dao.ConsultaDao;
import com.uel.bd1.service.CadastroService;
import com.uel.bd1.model.DadosTela.PesquisaTela;
import com.uel.bd1.model.DadosTela.ParticipacaoTela;
import com.uel.bd1.model.PesquisaSalvarRequest;
import com.uel.bd1.model.Grupo;
import com.uel.bd1.model.Questao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pesquisas")
public class PesquisaController {

    private final PesquisaDao dao;
    private final ConsultaDao consulta;
    private final CadastroService cadastro;

    public PesquisaController(PesquisaDao dao, ConsultaDao consulta, CadastroService cadastro) {
        this.dao = dao;
        this.consulta = consulta;
        this.cadastro = cadastro;
    }

    @GetMapping
    public List<PesquisaTela> listar() throws SQLException {
        return consulta.listarPesquisas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesquisaTela> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        PesquisaTela registro = consulta.buscarPesquisa(id);

        if (registro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registro);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> inserir(
            @RequestBody PesquisaSalvarRequest dados) throws SQLException {
        int id = cadastro.salvarPesquisa(null, dados);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("id", id, "mensagem", "Pesquisa cadastrada com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("id") int id,
            @RequestBody PesquisaSalvarRequest dados) throws SQLException {
        cadastro.salvarPesquisa(id, dados);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable("id") int id) throws SQLException {

        boolean deletou = dao.deletar(id);

        if (!deletou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarDadosInvalidos(
            IllegalArgumentException e) {

        return ResponseEntity
            .badRequest()
            .body(e.getMessage());
    }

    @GetMapping("/{id}/participacoes")
    public List<ParticipacaoTela> listarParticipacoes(@PathVariable("id") int id) throws SQLException {
        return consulta.listarParticipacoesDaPesquisa(id);
    }

    @GetMapping("/{id}/grupos")
    public List<Grupo> listarGrupos(@PathVariable("id") int id) throws SQLException {
        return dao.listarGrupos(id);
    }

    @GetMapping("/{id}/questoes")
    public List<Questao> listarQuestoes(@PathVariable("id") int id) throws SQLException {
        return dao.listarQuestoesDaPesquisa(id);
    }

}
