package com.uel.bd1.model;

public record ParticipacaoPorGrupo(
    Integer idGrupo,
    String nomeGrupo,
    Long esperados,
    Long respondidos,
    Long pendentes
) {}
