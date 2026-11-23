package com.uel.bd1.model;

public record Resposta(
    Integer idResposta,
    Integer idPart,
    Integer idQuestao,
    String texto,
    Integer valorEscala
) {}
