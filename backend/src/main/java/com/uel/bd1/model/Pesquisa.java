package com.uel.bd1.model;

import java.util.List;

// listagem de questoes
public record Pesquisa(int id, String titulo, String descricao, String dataPesquisa,
                       String horaIni, String horaFim, boolean anonima,
                       int totalQuestoes, List<Integer> idsQuestoes) {

    public Pesquisa comQuestoes(List<Integer> ids) {
        return new Pesquisa(id, titulo, descricao, dataPesquisa, horaIni, horaFim,
                anonima, totalQuestoes, ids);
    }
}
