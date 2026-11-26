package com.uel.bd1.model;

import java.math.BigDecimal;

public record RankingQuestao(
    Long posicao,
    Integer idQuestao,
    String descricao,
    Integer idTema,
    String nomeTema,
    BigDecimal media,
    Long totalRespostas
) {}
