package com.uel.bd1.dao;

import com.uel.bd1.model.Tema;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TemaDao {

    private final DataSource dataSource;

    public TemaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Tema> listar() throws SQLException {
        List<Tema> registros = new ArrayList<>();
        String sql = """
            SELECT id_tema, nome_tema, descricao
            FROM tema
            ORDER BY id_tema
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new Tema(
                        resultado.getInt("id_tema"),
                        resultado.getString("nome_tema"),
                        resultado.getString("descricao")
                    ));
            }
        }
        return registros;
    }

    public Tema listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_tema, nome_tema, descricao
            FROM tema
            WHERE id_tema = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Tema(
                        resultado.getInt("id_tema"),
                        resultado.getString("nome_tema"),
                        resultado.getString("descricao")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(Tema dados) throws SQLException {
        validarTema(dados);

        String sql = """
            INSERT INTO tema (nome_tema, descricao)
            VALUES (?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeTema());
            comando.setString(2, dados.descricao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Tema com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Tema: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(Tema dados) throws SQLException {
        validarTema(dados);

        if (dados.idTema() == null || dados.idTema() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE tema
            SET nome_tema = ?, descricao = ?
            WHERE id_tema = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeTema());
            comando.setString(2, dados.descricao());
            comando.setInt(3, dados.idTema());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Tema com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Tema: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM tema WHERE id_tema = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Tema com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Tema: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    private void validarTema(Tema dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de Tema são obrigatórios.");
        }

        if (dados.nomeTema() == null || dados.nomeTema().isBlank() || dados.nomeTema().length() > 120) {
            throw new IllegalArgumentException("Informe um nome não vazio, com até 120 caracteres.");
        }
    }
}
