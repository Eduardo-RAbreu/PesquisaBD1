package com.uel.bd1.dao;

import com.uel.bd1.model.CadastroUsuarioRequest;
import com.uel.bd1.model.Usuario;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioDao {

    private final DataSource dataSource;

    public UsuarioDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT id_usu, nome, email
            FROM usuario
            ORDER BY id_usu
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                usuarios.add(new Usuario(
                    resultado.getInt("id_usu"),
                    resultado.getString("nome"),
                    resultado.getString("email")
                ));
            }
        }
        return usuarios;
    }

    public Usuario listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe um ID de usuário válido.");
        }

        String sql = """
            SELECT id_usu, nome, email
            FROM usuario
            WHERE id_usu = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(
                        resultado.getInt("id_usu"),
                        resultado.getString("nome"),
                        resultado.getString("email")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(CadastroUsuarioRequest dados) throws SQLException {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados do usuário são obrigatórios.");
        }
        validarDados(new Usuario(null, dados.nome(), dados.email()));

        if (dados.senha() == null || dados.senha().isBlank()
                || dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                "Informe uma senha não vazia."
            );
        }

        String sql = """
            INSERT INTO usuario (nome, email, senha_hash)
            VALUES (?, ?, crypt(?, gen_salt('bf', 10)))
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nome());
            comando.setString(2, dados.email());
            comando.setString(3, dados.senha());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um usuário com esse e-mail.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(Usuario dados) throws SQLException {
        validarDados(dados);

        if (dados.idUsu() == null || dados.idUsu() <= 0) {
            throw new IllegalArgumentException("Informe um ID de usuário válido para atualizar.");
        }

        String sql = """
            UPDATE usuario
            SET nome = ?, email = ?
            WHERE id_usu = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, dados.nome());
            comando.setString(2, dados.email());
            comando.setInt(3, dados.idUsu());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um usuário com esse e-mail.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe um ID de usuário válido para excluir.");
        }

        String sql = "DELETE FROM usuario WHERE id_usu = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                    "Não é possível excluir o usuário: existem registros que impedem a exclusão.", e
                );
            }
            throw e;
        }
    }

    public Usuario autenticar(String email, String senha) throws SQLException {
        if (senha == null || senha.isBlank()
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                "Informe uma senha não vazia."
            );
        }
        if (email == null || email.isBlank() || email.length() > 180) {
            throw new IllegalArgumentException("Informe um e-mail não vazio, com até 180 caracteres.");
        }

        String sql = """
                Select id_usu, nome, email
                from usuario
                where email = ? and senha_hash = crypt(?, senha_hash)
                """;
        
         try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setString(1, email);
            comando.setString(2, senha);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(
                        resultado.getInt("id_usu"),
                        resultado.getString("nome"),
                        resultado.getString("email")
                    );
                }
            }
        }
        return null;
        
    }


    private void validarDados(Usuario dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados do usuário são obrigatórios.");
        }
        if (dados.nome() == null || dados.nome().isBlank() || dados.nome().length() > 120) {
            throw new IllegalArgumentException("Informe um nome não vazio, com até 120 caracteres.");
        }
        if (dados.email() == null || dados.email().isBlank() || dados.email().length() > 180) {
            throw new IllegalArgumentException("Informe um e-mail não vazio, com até 180 caracteres.");
        }
    }
}
