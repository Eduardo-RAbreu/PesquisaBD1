package com.uel.bd1.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class DadosTela {
    private DadosTela() {}

    public record UsuarioTela(Integer id, String nome, String email, String grupos) {}
    public record GrupoTela(Integer id, String nomeGrupo, long totalUsuarios) {}
    public record ParticipacaoTela(Integer idPart, Integer idPesquisa, Integer idGrupoRespondente,
                                   Integer idUsuario, String nomeUsuario) {}
    public record TemaTela(Integer id, String nomeTema, String descricao, long totalQuestoes) {}
    public record TipoTela(Integer id, String nomeTipo, String descricao) {}
    public record QuestaoTela(Integer id, Integer idTema, Integer idTipoQuestao,
                             String descricao, String nomeTema, String nomeTipo, boolean ehTexto) {}
    public record PesquisaTela(Integer id, Boolean anonima, LocalDate dataPesquisa,
                               LocalTime horaIni, LocalTime horaFim, String titulo, String descricao,
                               List<Integer> idsQuestoes, List<Integer> idsGrupos,
                               long totalQuestoes, long totalGrupos) {}
    public record PesquisaDisponivel(Integer id, String titulo, String descricao, Boolean anonima,
                                    LocalDate dataPesquisa, LocalTime horaIni, LocalTime horaFim,
                                    boolean jaRespondeu, String situacao) {}
    public record QuestaoFormulario(Integer idQuestao, int ordem, String descricao,
                                   String nomeTema, String nomeTipo, boolean ehTexto) {}
    public record FormularioPesquisa(Integer id, String titulo, String descricao, Boolean anonima,
                                    List<QuestaoFormulario> questoes, List<GrupoTela> gruposRespondente) {}
}
