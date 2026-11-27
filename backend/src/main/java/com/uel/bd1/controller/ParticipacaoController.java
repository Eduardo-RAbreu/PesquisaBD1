package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.Participacao;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/participacoes")
public class ParticipacaoController {

    private final ParticipacaoDao dao;

    public ParticipacaoController(ParticipacaoDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Participacao> listar() throws SQLException {
        return dao.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Participacao> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        Participacao registro = dao.listarID(id);

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
