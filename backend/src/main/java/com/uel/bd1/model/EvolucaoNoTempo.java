package com.uel.bd1.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvolucaoNoTempo(
    Integer idPesquisa,
    String titulo,
    LocalDate dataPesquisa,
    BigDecimal media,
    Long participacoes,
    BigDecimal mediaGeral
) {}
