package com.uel.bd1.controller;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.model.PesquisaQuestao;

import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody PesquisaQuestao dados) throws SQLException {

        boolean inseriu = dao.inserirQuestao(dados);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o registro.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Registro cadastrado com sucesso.");
    }

    @PutMapping("/{idPesquisa}/{idQuestao}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("idPesquisa") int idPesquisa,
            @PathVariable("idQuestao") int idQuestao,
            @RequestBody PesquisaQuestao dados) throws SQLException {

        PesquisaQuestao registro = new PesquisaQuestao(
            idPesquisa,
            idQuestao,
            dados.ordemQuestao()
        );

        boolean atualizou = dao.atualizarQuestao(registro);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idPesquisa}/{idQuestao}")
    public ResponseEntity<Void> deletar(
            @PathVariable("idPesquisa") int idPesquisa,
            @PathVariable("idQuestao") int idQuestao) throws SQLException {

        boolean deletou = dao.deletarQuestao(idPesquisa, idQuestao);

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
}
