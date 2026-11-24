package com.uel.bd1.controller;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.model.Resposta;

import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody Resposta dados) throws SQLException {

        boolean inseriu = dao.inserirResposta(dados);

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
            @RequestBody Resposta dados) throws SQLException {

        Resposta registro = new Resposta(
            id,
            dados.idPart(),
            dados.idQuestao(),
            dados.texto(),
            dados.valorEscala()
        );

        boolean atualizou = dao.atualizarResposta(registro);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable("id") int id) throws SQLException {

        boolean deletou = dao.deletarResposta(id);

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
