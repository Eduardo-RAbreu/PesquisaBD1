package com.uel.bd1.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.uel.bd1.model.Questao;

@Repository
public class QuestaoDao {

    // Junta questao com tema e tipo_questao
    private static final String SELECT_BASE = """
            SELECT q.id_questao, q.descricao,
                   q.id_tema, t.nome_tema,
                   q.id_tipo_questao, tp.nome_tipo
              FROM questao q
              JOIN tema t ON t.id_tema = q.id_tema
              JOIN tipo_questao tp ON tp.id_tipo_questao = q.id_tipo_questao
            """;

    private static final RowMapper<Questao> MAPPER = (rs, i) -> new Questao(
            rs.getInt("id_questao"),
            rs.getString("descricao"),
            rs.getInt("id_tema"),
            rs.getString("nome_tema"),
            rs.getInt("id_tipo_questao"),
            rs.getString("nome_tipo"));

    private final JdbcTemplate jdbc;

    public QuestaoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Questao> listar() {
        return jdbc.query(SELECT_BASE + " ORDER BY q.id_questao", MAPPER);
    }

    public Optional<Questao> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE q.id_questao = ?", MAPPER, id)
                .stream().findFirst();
    }

    public int inserir(String descricao, int idTema, int idTipoQuestao) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO questao (descricao, id_tema, id_tipo_questao)
                VALUES (?, ?, ?)
                RETURNING id_questao
                """,
                Integer.class, descricao, idTema, idTipoQuestao);
        return id;
    }

    public void atualizar(int id, String descricao, int idTema, int idTipoQuestao) {
        jdbc.update(
                "UPDATE questao SET descricao = ?, id_tema = ?, id_tipo_questao = ? WHERE id_questao = ?",
                descricao, idTema, idTipoQuestao, id);
    }

    public void excluir(int id) {
        jdbc.update("DELETE FROM questao WHERE id_questao = ?", id);
    }
}
