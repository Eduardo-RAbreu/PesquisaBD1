package com.uel.bd1.model;

import java.util.List;

// Corpo do JSON recebido ao cadastrar ou editar uma pesquisa
public record PesquisaRequest(String titulo, String descricao,
                     String dataPesquisa, String horaIni, String horaFim, 
                     boolean anonima, List<Integer> idsQuestoes) {
}
