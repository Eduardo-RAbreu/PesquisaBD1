package com.uel.bd1.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Pesquisa(
    Integer idPesquisa,
    Boolean anonima,
    LocalDate dataPesquisa,
    LocalTime horaIni,
    LocalTime horaFim,
    String titulo,
    String descricao
) {}