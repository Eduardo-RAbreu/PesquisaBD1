package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.Resposta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/respostas")
public class RespostaController {

    private final ParticipacaoDao dao;

    public RespostaController(ParticipacaoDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Resposta> listar() throws SQLException {
        return dao.listarResposta();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resposta> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        Resposta registro = dao.listarRespostaID(id);

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
