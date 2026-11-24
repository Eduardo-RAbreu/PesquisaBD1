package com.uel.bd1.controller;

import com.uel.bd1.dao.TemaDao;
import com.uel.bd1.model.Tema;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController 
@RequestMapping("api/temas")
public class TemaController {
    private final TemaDao temad;

    public TemaController(TemaDao t){
        this.temad = t;
    }

    @GetMapping
    public List<Tema> listar() throws SQLException{
        return temad.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tema> buscarPorId( @PathVariable("id") int id) throws SQLException{
        Tema tt = temad.listarID(id);

        if(tt==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tt);
    }

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody Tema t) throws SQLException {

        boolean inseriu = temad.inserir(t);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o tema.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Tema cadastrada com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable("id") int id, @RequestBody Tema dados) throws SQLException {

        Tema t= new Tema(
            id,
            dados.nomeTema(),
            dados.descricao()
        );

        boolean atualizou = temad.atualizar(t);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) throws SQLException {

        boolean deletou = temad.deletar(id);

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
