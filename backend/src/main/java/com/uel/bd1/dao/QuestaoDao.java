package com.uel.bd1.dao;

import com.uel.bd1.model.Questao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QuestaoDao {

    private final DataSource dataSource;

    public QuestaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Questao> listar() throws SQLException {
        List<Questao> questoes = new ArrayList<>();

        String sql = """
            SELECT id_questao, id_tema, id_tipo_questao, descricao
            FROM questao
            ORDER BY id_questao
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                Questao questao = new Questao(
                    resultado.getInt("id_questao"),
                    resultado.getInt("id_tema"),
                    resultado.getInt("id_tipo_questao"),
                    resultado.getString("descricao")
                );

                questoes.add(questao);
            }
        }

        return questoes;
    }

    public Questao listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de questão válido."
            );
        }

        String sql = """
            SELECT id_questao, id_tema, id_tipo_questao, descricao
            FROM questao
            WHERE id_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Questao(
                        resultado.getInt("id_questao"),
                        resultado.getInt("id_tema"),
                        resultado.getInt("id_tipo_questao"),
                        resultado.getString("descricao")
                    );
                }
            }
        }

        return null;
    }

    public boolean inserir(Questao q) throws SQLException {
        validarDados(q);

        String sql = """
            INSERT INTO questao (id_tema, id_tipo_questao, descricao)
            VALUES (?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, q.idTema());
            comando.setInt(2, q.idTipoQuestao());
            comando.setString(3, q.descricao());

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "O tema ou o tipo de questão informado não existe.",
                    e
                );
            }

            throw e;
        }
    }

    public boolean atualizar(Questao q) throws SQLException {
        validarDados(q);

        if (q.idQuestao() == null || q.idQuestao() <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de questão válido para atualizar."
            );
        }

        String sql = """
            UPDATE questao
            SET id_tema = ?,
                id_tipo_questao = ?,
                descricao = ?
            WHERE id_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, q.idTema());
            comando.setInt(2, q.idTipoQuestao());
            comando.setString(3, q.descricao());
            comando.setInt(4, q.idQuestao());

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "O tema ou o tipo de questão informado não existe.",
                    e
                );
            }

            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de questão válido para excluir."
            );
        }

        String sql = """
            DELETE FROM questao
            WHERE id_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Não é possível excluir uma questão vinculada "
                    + "a pesquisas ou respostas.",
                    e
                );
            }

            throw e;
        }
    }

    private void validarDados(Questao q) {
        if (q == null) {
            throw new IllegalArgumentException(
                "Os dados da questão são obrigatórios."
            );
        }

        if (q.descricao() == null || q.descricao().isBlank()) {
            throw new IllegalArgumentException(
                "A descrição da questão é obrigatória."
            );
        }

        if (q.idTema() == null || q.idTema() <= 0) {
            throw new IllegalArgumentException(
                "Informe um tema válido."
            );
        }

        if (q.idTipoQuestao() == null || q.idTipoQuestao() <= 0) {
            throw new IllegalArgumentException(
                "Informe um tipo de questão válido."
            );
        }
    }
}
