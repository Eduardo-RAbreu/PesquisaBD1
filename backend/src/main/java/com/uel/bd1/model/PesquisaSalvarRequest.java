package com.uel.bd1.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record PesquisaSalvarRequest(
    Boolean anonima,
    LocalDate dataPesquisa,
    LocalTime horaIni,
    LocalTime horaFim,
    String titulo,
    String descricao,
    List<Integer> idsQuestoes,
    List<Integer> idsGrupos
) {}
