package com.uel.bd1.model;

import java.math.BigDecimal;

public record MediaPorTema(
    Integer idTema,
    String nomeTema,
    BigDecimal media,
    Long totalRespostas
) {}
