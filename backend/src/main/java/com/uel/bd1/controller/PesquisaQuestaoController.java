package com.uel.bd1.controller;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.model.PesquisaQuestao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/pesquisas-questoes")
public class PesquisaQuestaoController {

    private final PesquisaDao dao;

    public PesquisaQuestaoController(PesquisaDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<PesquisaQuestao> listar() throws SQLException {
        return dao.listarQuestao();
    }

    @GetMapping("/{idPesquisa}/{idQuestao}")
    public ResponseEntity<PesquisaQuestao> buscarPorId(
            @PathVariable("idPesquisa") int idPesquisa,
            @PathVariable("idQuestao") int idQuestao) throws SQLException {

        PesquisaQuestao registro = dao.listarQuestaoID(idPesquisa, idQuestao);

        if (registro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarDadosInvalidos(
            IllegalArgumentException e) {

        return ResponseEntity
            .badRequest()
            .body(e.getMessage());
    }
}
