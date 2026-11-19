package com.uel.bd1.model;

// Questão já com o nome do tema e do tipo (resultado do JOIN)
public record Questao(int id, String descricao, int idTema, String nomeTema,
                      int idTipoQuestao, String nomeTipo) {
}
