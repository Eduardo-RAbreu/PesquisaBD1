package com.uel.bd1.model;

public record Questao(
    Integer idQuestao,
    Integer idTema,
    Integer idTipoQuestao,
    String descricao
) {}