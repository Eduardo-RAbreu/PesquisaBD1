package com.uel.bd1.dao;

import com.uel.bd1.model.Grupo;
import com.uel.bd1.model.Usuario;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GrupoDao {

    private final DataSource dataSource;

    public GrupoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Grupo> listar() throws SQLException {
        List<Grupo> registros = new ArrayList<>();
        String sql = """
            SELECT id_grupo, nome_grupo
            FROM grupo
            ORDER BY id_grupo
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new Grupo(
                        resultado.getInt("id_grupo"),
                        resultado.getString("nome_grupo")
                    ));
            }
        }
        return registros;
    }

    public Grupo listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_grupo, nome_grupo
            FROM grupo
            WHERE id_grupo = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Grupo(
                        resultado.getInt("id_grupo"),
                        resultado.getString("nome_grupo")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(Grupo dados) throws SQLException {
        validarGrupo(dados);

        String sql = """
            INSERT INTO grupo (nome_grupo)
            VALUES (?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeGrupo());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Grupo com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Grupo: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(Grupo dados) throws SQLException {
        validarGrupo(dados);

        if (dados.idGrupo() == null || dados.idGrupo() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE grupo
            SET nome_grupo = ?
            WHERE id_grupo = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nomeGrupo());
            comando.setInt(2, dados.idGrupo());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Grupo com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Grupo: existem registros vinculados.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM grupo WHERE id_grupo = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um Grupo com esse nome.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir Grupo: existem registros vinculados.", e);
            }
            throw e;
        }
    }


    public boolean adicionarUsuario(int idGrupo, int idUsuario) throws SQLException{
        if(idGrupo <= 0  || idUsuario <=0){
            throw new IllegalArgumentException("Informe IDs positivos para a consulta.");
        }

        String sql ="""
                Insert into grupo_usuario(id_usu, id_grupo)
                values(?, ?)
                """;
        
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idUsuario);
            comando.setInt(2, idGrupo);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                "Esse usuário já pertence ao grupo.", e
                );
            }

            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                "O usuário ou o grupo informado não existe.", e
                );
            }

            throw e;
        }
    }

    public boolean removerUsuario(int idGrupo, int idUsuario) throws SQLException{
        if(idGrupo <= 0  || idUsuario <=0){
            throw new IllegalArgumentException("Informe IDs positivos para a consulta.");
        }

        String sql ="""
                Delete from grupo_usuario
                where id_usu = ? and id_grupo = ?
                """;
    
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idUsuario);
            comando.setInt(2, idGrupo);
            return comando.executeUpdate() > 0;
        } 
    }

    public List<Usuario> listarUsuarios(int idGrupo) throws SQLException {

        if (idGrupo <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de grupo positivo."
            );
        }

        List<Usuario> registros = new ArrayList<>();

        String sql = """
            SELECT u.id_usu, u.nome, u.email
            FROM grupo_usuario g
            JOIN usuario u ON u.id_usu = g.id_usu
            WHERE g.id_grupo = ?
            ORDER BY u.nome, u.id_usu
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idGrupo);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                        registros.add(new Usuario(
                        resultado.getInt("id_usu"),
                        resultado.getString("nome"),
                        resultado.getString("email")
                    ));
                }
            }
        }

        return registros;
    }

    private void validarGrupo(Grupo dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de Grupo são obrigatórios.");
        }

        if (dados.nomeGrupo() == null || dados.nomeGrupo().isBlank() || dados.nomeGrupo().length() > 120) {
            throw new IllegalArgumentException("Informe um nome não vazio, com até 120 caracteres.");
        }
    }
}
