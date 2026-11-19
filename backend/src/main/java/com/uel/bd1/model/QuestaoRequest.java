package com.uel.bd1.model;

// Corpo do JSON recebido ao cadastrar ou editar uma questão
public record QuestaoRequest(String descricao, Integer idTema, Integer idTipoQuestao) {
}
