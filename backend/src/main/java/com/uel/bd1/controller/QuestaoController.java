package com.uel.bd1.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uel.bd1.dao.QuestaoDao;
import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.Questao;
import com.uel.bd1.model.QuestaoRequest;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoDao questaoDao;

    public QuestaoController(QuestaoDao questaoDao) {
        this.questaoDao = questaoDao;
    }

    @GetMapping
    public List<Questao> listar() {
        return questaoDao.listar();
    }

    @GetMapping("/{id}")
    public Questao buscar(@PathVariable int id) {
        return questaoDao.buscarPorId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> criar(@RequestBody QuestaoRequest req) {
        validar(req);
        try {
            int id = questaoDao.inserir(req.descricao().trim(), req.idTema(), req.idTipoQuestao());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tema ou tipo de questão inválido.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable int id, @RequestBody QuestaoRequest req) {
        validar(req);
        questaoDao.buscarPorId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Questão não encontrada."));
        try {
            questaoDao.atualizar(id, req.descricao().trim(), req.idTema(), req.idTipoQuestao());
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tema ou tipo de questão inválido.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        try {
            questaoDao.excluir(id);
        } catch (DataIntegrityViolationException e) {
            // A chave estrangeira impede apagar questão que já está em uma pesquisa ou tem respostas.
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta questão já faz parte de uma pesquisa e não pode ser excluída.");
        }
        return ResponseEntity.noContent().build();
    }

    private void validar(QuestaoRequest req) {
        if (req.descricao() == null || req.descricao().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escreva o enunciado da questão.");
        }
        if (req.idTema() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha o tema da questão.");
        }
        if (req.idTipoQuestao() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha o tipo da questão.");
        }
    }
}
