package com.uel.bd1.dao;

import com.uel.bd1.model.TipoQuestao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TipoQuestaoDao {

    private final DataSource dataSource;

    public TipoQuestaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<TipoQuestao> listar() throws SQLException {
        List<TipoQuestao> registros = new ArrayList<>();
        String sql = """
            SELECT id_tipo_questao, nome_tipo, descricao
            FROM tipo_questao
            ORDER BY id_tipo_questao
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new TipoQuestao(
                        resultado.getInt("id_tipo_questao"),
                        resultado.getString("nome_tipo"),
                        resultado.getString("descricao")
                    ));
            }
        }
        return registros;
    }

    public TipoQuestao listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_tipo_questao, nome_tipo, descricao
            FROM tipo_questao
            WHERE id_tipo_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new TipoQuestao(
                        resultado.getInt("id_tipo_questao"),
                        resultado.getString("nome_tipo"),
                        resultado.getString("descricao")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(TipoQuestao dados) throws SQLException {
        validarTipoQuestao(dados);

        String sql = """
            INSERT INTO tipo_questao (nome_tipo, descricao)
            VALUES (?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeTipo());
            comando.setString(2, dados.descricao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um TipoQuestao com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir TipoQuestao: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(TipoQuestao dados) throws SQLException {
        validarTipoQuestao(dados);

        if (dados.idTipoQuestao() == null || dados.idTipoQuestao() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE tipo_questao
            SET nome_tipo = ?, descricao = ?
            WHERE id_tipo_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeTipo());
            comando.setString(2, dados.descricao());
            comando.setInt(3, dados.idTipoQuestao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um TipoQuestao com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir TipoQuestao: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM tipo_questao WHERE id_tipo_questao = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um TipoQuestao com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir TipoQuestao: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    private void validarTipoQuestao(TipoQuestao dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de TipoQuestao são obrigatórios.");
        }

        if (dados.nomeTipo() == null || dados.nomeTipo().isBlank() || dados.nomeTipo().length() > 30) {
            throw new IllegalArgumentException("Informe um nome não vazio, com até 30 caracteres.");
        }

        if (!"ESCALA".equals(dados.nomeTipo()) && !"TEXTO".equals(dados.nomeTipo())) {
            throw new IllegalArgumentException("O tipo deve ser ESCALA ou TEXTO.");
        }
    }
}
