package com.uel.bd1.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uel.bd1.model.Tema;

@Repository
public class TemaDao {

    private final JdbcTemplate jdbc;

    public TemaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Tema> listar() {
        return jdbc.query("""
                SELECT t.id_tema, t.nome_tema, t.descricao, COUNT(q.id_questao) AS total
                  FROM tema t
                  LEFT JOIN questao q ON q.id_tema = t.id_tema
                 GROUP BY t.id_tema
                 ORDER BY t.nome_tema
                """,
                (rs, i) -> new Tema(
                        rs.getInt("id_tema"),
                        rs.getString("nome_tema"),
                        rs.getString("descricao"),
                        rs.getInt("total")));
    }

    public int inserir(String nomeTema, String descricao) {
        Integer id = jdbc.queryForObject(
                "INSERT INTO tema (nome_tema, descricao) VALUES (?, ?) RETURNING id_tema",
                Integer.class, nomeTema, descricao);
        return id;
    }

    public void excluir(int id) {
        jdbc.update("DELETE FROM tema WHERE id_tema = ?", id);
    }
}
