package com.uel.bd1.model;

import java.time.LocalDate;
import java.time.LocalTime;

// Dados da pesquisa para gravar no banco
public record PesquisaDados(String titulo, String descricao, LocalDate dataPesquisa,
                            LocalTime horaIni, LocalTime horaFim, boolean anonima) {
}
