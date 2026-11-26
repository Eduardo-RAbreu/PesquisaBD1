package com.uel.bd1.dao;

import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.DadosTela.*;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;

@Repository
public class ConsultaDao {
    private final DataSource dataSource;

    public ConsultaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<UsuarioTela> listarUsuarios() throws SQLException {
        String sql = """
            SELECT u.id_usu, u.nome, u.email,
                   COALESCE(string_agg(g.nome_grupo, ', ' ORDER BY g.nome_grupo), '') AS grupos
            FROM usuario u
            LEFT JOIN grupo_usuario gu ON gu.id_usu = u.id_usu
            LEFT JOIN grupo g ON g.id_grupo = gu.id_grupo
            GROUP BY u.id_usu, u.nome, u.email
            ORDER BY u.nome, u.id_usu
            """;
        List<UsuarioTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                registros.add(new UsuarioTela(r.getInt("id_usu"), r.getString("nome"),
                    r.getString("email"), r.getString("grupos")));
            }
        }
        return registros;
    }

    public List<GrupoTela> listarGrupos() throws SQLException {
        String sql = """
            SELECT g.id_grupo, g.nome_grupo, COUNT(gu.id_usu) AS total
            FROM grupo g LEFT JOIN grupo_usuario gu ON gu.id_grupo = g.id_grupo
            GROUP BY g.id_grupo, g.nome_grupo ORDER BY g.nome_grupo
            """;
        List<GrupoTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) registros.add(new GrupoTela(r.getInt("id_grupo"), r.getString("nome_grupo"), r.getLong("total")));
        }
        return registros;
    }

    public List<TemaTela> listarTemas() throws SQLException {
        String sql = """
            SELECT t.id_tema, t.nome_tema, t.descricao, COUNT(q.id_questao) AS total
            FROM tema t LEFT JOIN questao q ON q.id_tema = t.id_tema
            GROUP BY t.id_tema, t.nome_tema, t.descricao ORDER BY t.nome_tema
            """;
        List<TemaTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) registros.add(new TemaTela(r.getInt("id_tema"), r.getString("nome_tema"),
                r.getString("descricao"), r.getLong("total")));
        }
        return registros;
    }

    public List<TipoTela> listarTipos() throws SQLException {
        List<TipoTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(
                "SELECT id_tipo_questao, nome_tipo, descricao FROM tipo_questao ORDER BY id_tipo_questao");
             ResultSet r = s.executeQuery()) {
            while (r.next()) registros.add(new TipoTela(r.getInt("id_tipo_questao"), r.getString("nome_tipo"), r.getString("descricao")));
        }
        return registros;
    }

    public List<QuestaoTela> listarQuestoes() throws SQLException {
        String sql = """
            SELECT q.id_questao, q.id_tema, q.id_tipo_questao, q.descricao, t.nome_tema, tq.nome_tipo
            FROM questao q JOIN tema t ON t.id_tema = q.id_tema
            JOIN tipo_questao tq ON tq.id_tipo_questao = q.id_tipo_questao ORDER BY q.id_questao
            """;
        List<QuestaoTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) registros.add(new QuestaoTela(r.getInt("id_questao"), r.getInt("id_tema"),
                r.getInt("id_tipo_questao"), r.getString("descricao"), r.getString("nome_tema"),
                r.getString("nome_tipo"), "TEXTO".equals(r.getString("nome_tipo"))));
        }
        return registros;
    }

    private static final String PESQUISAS = """
        SELECT p.*,
               (SELECT array_agg(pq.id_questao ORDER BY pq.ordem_questao)
                FROM pesquisa_questao pq WHERE pq.id_pesquisa = p.id_pesquisa) AS ids_questoes,
               (SELECT array_agg(gp.id_grupo ORDER BY gp.id_grupo)
                FROM grupo_pesquisa gp WHERE gp.id_pesquisa = p.id_pesquisa) AS ids_grupos
        FROM pesquisa p
        """;

    private List<Integer> lerIds(ResultSet r, String coluna) throws SQLException {
        java.sql.Array array = r.getArray(coluna);
        if (array == null) return List.of();
        try {
            List<Integer> ids = new ArrayList<>();
            for (Object valor : (Object[]) array.getArray()) ids.add(((Number) valor).intValue());
            return ids;
        } finally {
            array.free();
        }
    }

    private PesquisaTela lerPesquisa(ResultSet r) throws SQLException {
        List<Integer> questoes = lerIds(r, "ids_questoes");
        List<Integer> grupos = lerIds(r, "ids_grupos");
        return new PesquisaTela(r.getInt("id_pesquisa"), r.getBoolean("anonima"),
            r.getObject("data_pesquisa", LocalDate.class), r.getObject("hora_ini", LocalTime.class),
            r.getObject("hora_fim", LocalTime.class), r.getString("titulo"), r.getString("descricao"),
            questoes, grupos, questoes.size(), grupos.size());
    }

    public List<PesquisaTela> listarPesquisas() throws SQLException {
        List<PesquisaTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(PESQUISAS + " ORDER BY p.id_pesquisa");
             ResultSet r = s.executeQuery()) {
            while (r.next()) registros.add(lerPesquisa(r));
        }
        return registros;
    }

    public PesquisaTela buscarPesquisa(int id) throws SQLException {
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(PESQUISAS + " WHERE p.id_pesquisa = ?")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) { return r.next() ? lerPesquisa(r) : null; }
        }
    }

    public List<ParticipacaoTela> listarParticipacoesDaPesquisa(int idPesquisa) throws SQLException {
        if (idPesquisa <= 0) throw new IllegalArgumentException("Informe um ID de pesquisa positivo.");
        if (buscarPesquisa(idPesquisa) == null) throw new ApiException(404, "Pesquisa não encontrada.");
        String sql = """
            SELECT par.id_part, par.id_pesquisa, par.id_grupo_respondente,
                   u.id_usu, u.nome
            FROM participacao par
            JOIN pesquisa p ON p.id_pesquisa = par.id_pesquisa
            LEFT JOIN participacao_identificada pi ON pi.id_part = par.id_part AND p.anonima = FALSE
            LEFT JOIN usuario u ON u.id_usu = pi.id_usu
            WHERE par.id_pesquisa = ?
            ORDER BY u.nome NULLS LAST, par.id_part
            """;
        List<ParticipacaoTela> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idPesquisa);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) registros.add(new ParticipacaoTela(r.getInt("id_part"), r.getInt("id_pesquisa"),
                    r.getObject("id_grupo_respondente", Integer.class), r.getObject("id_usu", Integer.class),
                    r.getString("nome")));
            }
        }
        return registros;
    }

    private String situacao(LocalDate data, LocalTime inicio, LocalTime fim) {
        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        if (agora.isBefore(data.atTime(inicio))) return "FUTURA";
        return agora.isBefore(data.atTime(fim)) ? "ABERTA" : "ENCERRADA";
    }

    public List<PesquisaDisponivel> listarDisponiveis(int idUsuario) throws SQLException {
        String sql = """
            SELECT p.*, COALESCE(cp.concluida, FALSE) AS concluida
            FROM pesquisa p
            LEFT JOIN controle_participacao cp ON cp.id_pesquisa = p.id_pesquisa AND cp.id_usu = ?
            WHERE EXISTS (
                SELECT 1 FROM grupo_pesquisa gp JOIN grupo_usuario gu ON gu.id_grupo = gp.id_grupo
                WHERE gp.id_pesquisa = p.id_pesquisa AND gu.id_usu = ?
            )
            ORDER BY p.data_pesquisa, p.hora_ini, p.id_pesquisa
            """;
        List<PesquisaDisponivel> registros = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idUsuario); s.setInt(2, idUsuario);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    LocalDate data = r.getObject("data_pesquisa", LocalDate.class);
                    LocalTime inicio = r.getObject("hora_ini", LocalTime.class), fim = r.getObject("hora_fim", LocalTime.class);
                    registros.add(new PesquisaDisponivel(r.getInt("id_pesquisa"), r.getString("titulo"),
                        r.getString("descricao"), r.getBoolean("anonima"), data, inicio, fim,
                        r.getBoolean("concluida"), situacao(data, inicio, fim)));
                }
            }
        }
        return registros;
    }

    public FormularioPesquisa formulario(int idUsuario, int idPesquisa) throws SQLException {
        PesquisaDisponivel pesquisa = listarDisponiveis(idUsuario).stream()
            .filter(p -> p.id() == idPesquisa).findFirst().orElse(null);
        if (pesquisa == null) {
            if (buscarPesquisa(idPesquisa) == null) throw new ApiException(404, "Pesquisa não encontrada.");
            throw new ApiException(403, "Esta pesquisa não foi destinada aos seus grupos.");
        }
        if (pesquisa.jaRespondeu()) throw new ApiException(409, "Você já respondeu a esta pesquisa.");
        if (!"ABERTA".equals(pesquisa.situacao())) throw new ApiException(403, "A pesquisa está fora do período permitido.");

        List<QuestaoFormulario> questoes = new ArrayList<>();
        List<GrupoTela> grupos = new ArrayList<>();
        try (Connection c = dataSource.getConnection()) {
            String sql = """
                SELECT q.id_questao, pq.ordem_questao, q.descricao, t.nome_tema, tq.nome_tipo
                FROM pesquisa_questao pq JOIN questao q ON q.id_questao = pq.id_questao
                JOIN tema t ON t.id_tema = q.id_tema JOIN tipo_questao tq ON tq.id_tipo_questao = q.id_tipo_questao
                WHERE pq.id_pesquisa = ? ORDER BY pq.ordem_questao
                """;
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, idPesquisa);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) questoes.add(new QuestaoFormulario(r.getInt("id_questao"), r.getInt("ordem_questao"),
                        r.getString("descricao"), r.getString("nome_tema"), r.getString("nome_tipo"), "TEXTO".equals(r.getString("nome_tipo"))));
                }
            }
            sql = """
                SELECT g.id_grupo, g.nome_grupo FROM grupo g
                JOIN grupo_usuario gu ON gu.id_grupo = g.id_grupo
                JOIN grupo_pesquisa gp ON gp.id_grupo = g.id_grupo
                WHERE gu.id_usu = ? AND gp.id_pesquisa = ? ORDER BY g.nome_grupo
                """;
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, idUsuario); s.setInt(2, idPesquisa);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) grupos.add(new GrupoTela(r.getInt("id_grupo"), r.getString("nome_grupo"), 0));
                }
            }
        }
        return new FormularioPesquisa(pesquisa.id(), pesquisa.titulo(), pesquisa.descricao(), pesquisa.anonima(), questoes, grupos);
    }
}
