package com.uel.bd1.model;

import java.util.List;

public record CadastroUsuarioRequest(
    String nome,
    String email,
    String senha,
    List<Integer> idsGrupos
) {
    public CadastroUsuarioRequest(String nome, String email, String senha) {
        this(nome, email, senha, List.of());
    }
}
