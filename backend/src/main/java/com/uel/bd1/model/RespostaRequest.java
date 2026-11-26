package com.uel.bd1.model;

public record RespostaRequest(
    Integer idQuestao,
    String texto,
    Integer valorEscala
) {}