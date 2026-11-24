package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.Participacao;

import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody Participacao dados) throws SQLException {

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
            @RequestBody Participacao dados) throws SQLException {

        Participacao registro = new Participacao(
            id,
            dados.idPesquisa(),
            dados.idGrupoRespondente(),
            dados.dataIni(),
            dados.dataFim()
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
