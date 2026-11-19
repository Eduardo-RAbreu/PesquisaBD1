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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uel.bd1.dao.TemaDao;
import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.Tema;
import com.uel.bd1.model.TemaRequest;

@RestController
@RequestMapping("/api/temas")
public class TemaController {

    private final TemaDao temaDao;

    public TemaController(TemaDao temaDao) {
        this.temaDao = temaDao;
    }

    @GetMapping
    public List<Tema> listar() {
        return temaDao.listar();
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> criar(@RequestBody TemaRequest req) {
        if (req.nomeTema() == null || req.nomeTema().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escreva o nome do tema.");
        }
        String descricao = (req.descricao() == null || req.descricao().isBlank())
                ? null : req.descricao().trim();
        try {
            int id = temaDao.inserir(req.nomeTema().trim(), descricao);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
        } catch (DataIntegrityViolationException e) {
            // a coluna nome_tema é UNIQUE no banco
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um tema com esse nome.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        try {
            temaDao.excluir(id);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Este tema tem questões cadastradas e não pode ser excluído.");
        }
        return ResponseEntity.noContent().build();
    }
}
