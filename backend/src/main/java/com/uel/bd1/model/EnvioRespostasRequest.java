package com.uel.bd1.model;


import java.util.List;

public record EnvioRespostasRequest(
    List<RespostaRequest> respostas,
    Integer idGrupoRespondente
) {
    public EnvioRespostasRequest(List<RespostaRequest> respostas) {
        this(respostas, null);
    }
}
