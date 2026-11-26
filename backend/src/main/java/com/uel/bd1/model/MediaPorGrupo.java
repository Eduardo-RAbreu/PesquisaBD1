package com.uel.bd1.model;

import java.math.BigDecimal;

public record MediaPorGrupo(
    Integer idGrupo,
    String nomeGrupo,
    BigDecimal media,
    Long totalRespostas
) {}
