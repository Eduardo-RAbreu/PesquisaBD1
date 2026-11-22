package com.uel.bd1.controller;

import com.uel.bd1.dao.QuestaoDao;
import com.uel.bd1.model.Questao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoDao questaoDao;

    public QuestaoController(QuestaoDao questaoDao) {
        this.questaoDao = questaoDao;
    }

    @GetMapping
    public List<Questao> listar() throws SQLException {
        return questaoDao.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questao> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        Questao questao = questaoDao.listarID(id);

        if (questao == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(questao);
    }

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody Questao questao) throws SQLException {

        boolean inseriu = questaoDao.inserir(questao);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar a questão.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Questão cadastrada com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable("id") int id,
            @RequestBody Questao dados) throws SQLException {

        Questao questao = new Questao(
            id,
            dados.idTema(),
            dados.idTipoQuestao(),
            dados.descricao()
        );

        boolean atualizou = questaoDao.atualizar(questao);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable("id") int id) throws SQLException {

        boolean deletou = questaoDao.deletar(id);

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