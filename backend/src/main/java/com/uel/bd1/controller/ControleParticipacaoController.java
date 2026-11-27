package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.ControleParticipacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/controles-participacao")
public class ControleParticipacaoController {

    private final ParticipacaoDao dao;

    public ControleParticipacaoController(ParticipacaoDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<ControleParticipacao> listar() throws SQLException {
        return dao.listarControle();
    }

    @GetMapping("/{idUsu}/{idPesquisa}")
    public ResponseEntity<ControleParticipacao> buscarPorId(
            @PathVariable("idUsu") int idUsu,
            @PathVariable("idPesquisa") int idPesquisa) throws SQLException {

        ControleParticipacao registro = dao.listarControleID(idUsu, idPesquisa);

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
