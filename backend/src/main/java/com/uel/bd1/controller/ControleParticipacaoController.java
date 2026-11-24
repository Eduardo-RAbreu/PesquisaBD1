package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.ControleParticipacao;

import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody ControleParticipacao dados) throws SQLException {

        boolean inseriu = dao.inserirControle(dados);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o registro.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Registro cadastrado com sucesso.");
    }

    @PutMapping("/{idUsu}/{idPesquisa}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("idUsu") int idUsu,
            @PathVariable("idPesquisa") int idPesquisa,
            @RequestBody ControleParticipacao dados) throws SQLException {

        ControleParticipacao registro = new ControleParticipacao(
            idUsu,
            idPesquisa,
            dados.concluida()
        );

        boolean atualizou = dao.atualizarControle(registro);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{idUsu}/{idPesquisa}")
    public ResponseEntity<Void> deletar(
            @PathVariable("idUsu") int idUsu,
            @PathVariable("idPesquisa") int idPesquisa) throws SQLException {

        boolean deletou = dao.deletarControle(idUsu, idPesquisa);

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
