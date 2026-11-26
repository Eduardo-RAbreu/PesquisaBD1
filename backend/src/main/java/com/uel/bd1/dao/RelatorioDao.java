package com.uel.bd1.dao;

import com.uel.bd1.model.*;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RelatorioDao {
    private final DataSource dataSource;

    public RelatorioDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<MediaPorTema> mediaPorTema() throws SQLException {
        return mediaPorTema(null, null, 1);
    }

    public List<MediaPorTema> mediaPorTema(LocalDate de, LocalDate ate, int minimo) throws SQLException {
        String sql = """
            Select
                t.id_tema,
                t.nome_tema,
                coalesce(round(avg(r.valor_escala), 2), 0) as media,
                count(r.id_resposta) as total_respostas
            from tema t
            join questao q on q.id_tema = t.id_tema
            join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
            join resposta r on r.id_questao = q.id_questao
            join participacao par on par.id_part = r.id_part
            join pesquisa p on p.id_pesquisa = par.id_pesquisa
            where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
            """;
        if (de != null) sql += " and p.data_pesquisa >= ?";
        if (ate != null) sql += " and p.data_pesquisa <= ?";
        sql += """
             group by t.id_tema, t.nome_tema
             having count(r.id_resposta) >= ?
             order by t.nome_tema, t.id_tema
            """;

        List<MediaPorTema> registros = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            int parametro = 1;
            if (de != null) comando.setObject(parametro++, de);
            if (ate != null) comando.setObject(parametro++, ate);
            comando.setInt(parametro, minimo);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    registros.add(new MediaPorTema(
                        resultado.getInt("id_tema"),
                        resultado.getString("nome_tema"),
                        resultado.getBigDecimal("media"),
                        resultado.getLong("total_respostas")
                    ));
                }
            }
        }
        return registros;
    }

    public List<MediaPorGrupo> mediaPorGrupo(Integer idPesquisa) throws SQLException {
        String sql = """
            Select
                g.id_grupo,
                g.nome_grupo,
                coalesce(round(avg(r.valor_escala), 2), 0) as media,
                count(r.valor_escala) as total_respostas
            from grupo g
            left join participacao par on par.id_grupo_respondente = g.id_grupo
            """;
        if (idPesquisa != null) sql += " and par.id_pesquisa = ?";
        sql += """
             left join resposta r on r.id_part = par.id_part
                and r.id_questao in (
                    select q.id_questao
                    from questao q
                    join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
                    where tq.nome_tipo = 'ESCALA'
                )
            """;
        if (idPesquisa != null) {
            sql += """
                 where g.id_grupo in (
                    select gp.id_grupo from grupo_pesquisa gp where gp.id_pesquisa = ?
                 )
                """;
        }
        sql += " group by g.id_grupo, g.nome_grupo order by g.nome_grupo, g.id_grupo";

        List<MediaPorGrupo> registros = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            if (idPesquisa != null) {
                comando.setInt(1, idPesquisa);
                comando.setInt(2, idPesquisa);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    registros.add(new MediaPorGrupo(
                        resultado.getInt("id_grupo"),
                        resultado.getString("nome_grupo"),
                        resultado.getBigDecimal("media"),
                        resultado.getLong("total_respostas")
                    ));
                }
            }
        }
        return registros;
    }

    public List<EvolucaoNoTempo> evolucao(Integer idTema) throws SQLException {
        String sql = """
            with notas as (
                select p.id_pesquisa, p.titulo, p.data_pesquisa, par.id_part, r.valor_escala
                from pesquisa p
                join participacao par on par.id_pesquisa = p.id_pesquisa
                join resposta r on r.id_part = par.id_part
                join questao q on q.id_questao = r.id_questao
                join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
                where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
            """;
        if (idTema != null) sql += " and q.id_tema = ?";
        sql += """
            )
            select
                id_pesquisa,
                titulo,
                data_pesquisa,
                coalesce(round(avg(valor_escala), 2), 0) as media,
                count(distinct id_part) as participacoes,
                coalesce(round((select avg(valor_escala) from notas), 2), 0) as media_geral
            from notas
            group by id_pesquisa, titulo, data_pesquisa
            order by data_pesquisa, id_pesquisa
            """;

        List<EvolucaoNoTempo> registros = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            if (idTema != null) comando.setInt(1, idTema);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    registros.add(new EvolucaoNoTempo(
                        resultado.getInt("id_pesquisa"),
                        resultado.getString("titulo"),
                        resultado.getObject("data_pesquisa", LocalDate.class),
                        resultado.getBigDecimal("media"),
                        resultado.getLong("participacoes"),
                        resultado.getBigDecimal("media_geral")
                    ));
                }
            }
        }
        return registros;
    }

    public List<RankingQuestao> ranking(boolean melhores, String busca, List<Integer> idsTemas,
            int minimo, int limite) throws SQLException {
        String ordem = melhores ? "desc" : "asc";
        String sql = """
            Select
                rank() over (order by avg(r.valor_escala) %s) as posicao,
                q.id_questao,
                q.descricao,
                t.id_tema,
                t.nome_tema,
                coalesce(round(avg(r.valor_escala), 2), 0) as media,
                count(r.id_resposta) as total_respostas
            from questao q
            join tema t on t.id_tema = q.id_tema
            join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
            join resposta r on r.id_questao = q.id_questao
            where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
              and lower(q.descricao) like lower(?) escape '!'
            """.formatted(ordem);
        if (!idsTemas.isEmpty()) {
            String parametros = "?";
            for (int i = 1; i < idsTemas.size(); i++) parametros += ", ?";
            sql += " and q.id_tema in (" + parametros + ")";
        }
        sql += """
             group by q.id_questao, q.descricao, t.id_tema, t.nome_tema
             having count(r.id_resposta) >= ?
             order by posicao, q.id_questao
             limit ?
            """;

        List<RankingQuestao> registros = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            String palavra = busca.replace("!", "!!").replace("%", "!%").replace("_", "!_");
            comando.setString(1, "%" + palavra + "%");
            int parametro = 2;
            for (Integer idTema : idsTemas) comando.setInt(parametro++, idTema);
            comando.setInt(parametro++, minimo);
            comando.setInt(parametro, limite);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    registros.add(new RankingQuestao(
                        resultado.getLong("posicao"),
                        resultado.getInt("id_questao"),
                        resultado.getString("descricao"),
                        resultado.getInt("id_tema"),
                        resultado.getString("nome_tema"),
                        resultado.getBigDecimal("media"),
                        resultado.getLong("total_respostas")
                    ));
                }
            }
        }
        return registros;
    }

    public AdesaoPesquisa adesao(int idPesquisa) throws SQLException {
        try (Connection conexao = dataSource.getConnection()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            conexao.setReadOnly(true);
            conexao.setAutoCommit(false);
            try {
                AdesaoPesquisa relatorio = new AdesaoPesquisa(listarGrupos(conexao, idPesquisa),
                    listarPendentes(conexao, idPesquisa));
                conexao.commit();
                return relatorio;
            } catch (SQLException | RuntimeException e) {
                try { conexao.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }

    private List<ParticipacaoPorGrupo> listarGrupos(Connection conexao, int idPesquisa) throws SQLException {
        String sql = """
            SELECT g.id_grupo, g.nome_grupo, count(gu.id_usu) as esperados,
                   count(case when cp.concluida = true then gu.id_usu end) as respondidos,
                   count(gu.id_usu) - count(case when cp.concluida = true then gu.id_usu end) as pendentes
            FROM grupo_pesquisa gp
            JOIN grupo g ON g.id_grupo = gp.id_grupo
            LEFT JOIN grupo_usuario gu ON gu.id_grupo = g.id_grupo
            LEFT JOIN controle_participacao cp ON cp.id_usu = gu.id_usu AND cp.id_pesquisa = gp.id_pesquisa
            WHERE gp.id_pesquisa = ?
            GROUP BY g.id_grupo, g.nome_grupo
            ORDER BY g.nome_grupo, g.id_grupo
            """;
        List<ParticipacaoPorGrupo> registros = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idPesquisa);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) registros.add(new ParticipacaoPorGrupo(
                    resultado.getInt("id_grupo"), resultado.getString("nome_grupo"),
                    resultado.getLong("esperados"), resultado.getLong("respondidos"), resultado.getLong("pendentes")));
            }
        }
        return registros;
    }

    private List<UsuarioPendente> listarPendentes(Connection conexao, int idPesquisa) throws SQLException {
        String sql = """
            SELECT DISTINCT u.id_usu, u.nome, u.email
            FROM grupo_pesquisa gp
            JOIN grupo_usuario gu ON gu.id_grupo = gp.id_grupo
            JOIN usuario u ON u.id_usu = gu.id_usu
            LEFT JOIN controle_participacao cp ON cp.id_usu = u.id_usu AND cp.id_pesquisa = gp.id_pesquisa
            WHERE gp.id_pesquisa = ? AND (cp.concluida = false OR cp.id_usu IS NULL)
            ORDER BY u.nome, u.id_usu
            """;
        List<UsuarioPendente> registros = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idPesquisa);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) registros.add(new UsuarioPendente(
                    resultado.getInt("id_usu"), resultado.getString("nome"), resultado.getString("email")));
            }
        }
        return registros;
    }
}
