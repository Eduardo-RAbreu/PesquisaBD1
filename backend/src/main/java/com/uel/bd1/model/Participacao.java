package com.uel.bd1.model;

import java.time.LocalDateTime;

public record Participacao(
    Integer idPart,
    Integer idPesquisa,
    Integer idGrupoRespondente,
    LocalDateTime dataIni,
    LocalDateTime dataFim
) {}
