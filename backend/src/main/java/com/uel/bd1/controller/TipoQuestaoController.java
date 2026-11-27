package com.uel.bd1.controller;

import com.uel.bd1.dao.TipoQuestaoDao;
import com.uel.bd1.dao.ConsultaDao;
import com.uel.bd1.model.DadosTela.TipoTela;
import com.uel.bd1.model.TipoQuestao;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> tratarDadosInvalidos(
            IllegalArgumentException e) {

        return ResponseEntity
            .badRequest()
            .body(e.getMessage());
    }
}
