package com.uel.bd1.controller;

import com.uel.bd1.dao.TipoQuestaoDao;
import com.uel.bd1.dao.ConsultaDao;
import com.uel.bd1.model.DadosTela.TipoTela;
import com.uel.bd1.model.TipoQuestao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping({"/api/tipos", "/api/tipos-questao"})
public class TipoQuestaoController {

    private final TipoQuestaoDao dao;
    private final ConsultaDao consulta;

    public TipoQuestaoController(TipoQuestaoDao dao, ConsultaDao consulta) {
        this.dao = dao;
        this.consulta = consulta;
    }

    @GetMapping
    public List<TipoTela> listar() throws SQLException {
        return consulta.listarTipos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoQuestao> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        TipoQuestao registro = dao.listarID(id);

        if (registro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registro);
    }

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody TipoQuestao dados) throws SQLException {

        boolean inseriu = dao.inserir(dados);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o registro.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Registro cadastrado com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("id") int id,
            @RequestBody TipoQuestao dados) throws SQLException {

        TipoQuestao registro = new TipoQuestao(
            id,
            dados.nomeTipo(),
            dados.descricao()
        );

        boolean atualizou = dao.atualizar(registro);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

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
}
