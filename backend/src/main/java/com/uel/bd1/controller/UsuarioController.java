package com.uel.bd1.controller;

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

import com.uel.bd1.dao.UsuarioDao;
import com.uel.bd1.model.CadastroUsuarioRequest;
import com.uel.bd1.model.Usuario;

import java.sql.SQLException;
import java.util.List;

@RestController 
@RequestMapping("api/usuarios")
public class UsuarioController {
    private final UsuarioDao usud;

    public UsuarioController(UsuarioDao u){
        this.usud= u;
    }

    @GetMapping
    public List<Usuario> listar() throws SQLException{
        return usud.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId( @PathVariable("id") int id) throws SQLException{
        Usuario uu = usud.listarID(id);

        if(uu==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(uu);
    }

    @PostMapping
    public ResponseEntity<String> inserir(
            @RequestBody CadastroUsuarioRequest t) throws SQLException {

        boolean inseriu = usud.inserir(t);

        if (!inseriu) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Não foi possível cadastrar o Usuario.");
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body("Usuario cadastrada com sucesso.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable("id") int id, @RequestBody Usuario dados) throws SQLException {

        Usuario u= new Usuario(
            id,
            dados.nome(),
            dados.email()
        );

        boolean atualizou = usud.atualizar(u);

        if (!atualizou) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") int id) throws SQLException {

        boolean deletou = usud.deletar(id);

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
