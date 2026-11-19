package com.uel.bd1.dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.uel.bd1.model.TipoQuestao;

@Repository
public class TipoQuestaoDao {

    private final JdbcTemplate jdbc;

    public TipoQuestaoDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TipoQuestao> listar() {
        return jdbc.query(
                "SELECT id_tipo_questao, nome_tipo, descricao FROM tipo_questao ORDER BY id_tipo_questao",
                (rs, i) -> new TipoQuestao(
                        rs.getInt("id_tipo_questao"),
                        rs.getString("nome_tipo"),
                        rs.getString("descricao")));
    }
}
