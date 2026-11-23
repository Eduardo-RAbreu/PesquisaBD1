package com.uel.bd1.model;

public record CadastroUsuarioRequest(
    String nome,
    String email,
    String senha
) {}
