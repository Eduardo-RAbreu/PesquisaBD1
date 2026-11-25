package com.uel.bd1.controller;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.model.Grupo;
import com.uel.bd1.model.Pesquisa;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/pesquisas")
public class PesquisaController {

    private final PesquisaDao dao;

    public PesquisaController(PesquisaDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public List<Pesquisa> listar() throws SQLException {
        return dao.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pesquisa> buscarPorId(
            @PathVariable("id") int id) throws SQLException {

        Pesquisa registro = dao.listarID(id);

        if (registro == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(registro);
    }

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody Pesquisa dados) throws SQLException {

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
            @RequestBody Pesquisa dados) throws SQLException {

        Pesquisa registro = new Pesquisa(
            id,
            dados.anonima(),
            dados.dataPesquisa(),
            dados.horaIni(),
            dados.horaFim(),
            dados.titulo(),
            dados.descricao()
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

    @GetMapping("/{id}/grupos")
    public List<Grupo> listarGrupos(@PathVariable("id") int id) throws SQLException {
        return dao.listarGrupos(id);
    }

    @PostMapping("/{id}/{idg}")
    public ResponseEntity<String> inserirUsuario(@PathVariable("id") int id, @PathVariable("idg") int idg) throws SQLException {

        boolean inseriu = dao.adicionarGrupo(idg, id);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o grupo na pesquisa.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Grupo cadastrado com sucesso na pesquisa.");
    }

     @DeleteMapping("/{id}/{idg}")
    public ResponseEntity<Void> deletarGrupo(@PathVariable("id") int id, @PathVariable("idg") int idg) throws SQLException {

        boolean deletou = dao.removerGrupo(idg, id);

        if (!deletou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
