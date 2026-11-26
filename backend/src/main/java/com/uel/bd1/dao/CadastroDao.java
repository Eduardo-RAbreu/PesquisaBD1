package com.uel.bd1.dao;

import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.CadastroUsuarioRequest;
import com.uel.bd1.model.PesquisaSalvarRequest;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.List;

@Repository
public class CadastroDao {
    public int inserirUsuario(Connection c, CadastroUsuarioRequest dados) throws SQLException {
        String sql = "INSERT INTO usuario(nome,email,senha_hash) VALUES(?,?,crypt(?,gen_salt('bf',10))) RETURNING id_usu";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, dados.nome().trim()); s.setString(2, dados.email().trim()); s.setString(3, dados.senha());
            try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    public void adicionarGruposUsuario(Connection c, int id, List<Integer> grupos) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO grupo_usuario(id_usu,id_grupo) VALUES(?,?)")) {
            for (int grupo : grupos) { s.setInt(1, id); s.setInt(2, grupo); s.addBatch(); }
            s.executeBatch();
        }
    }

    public void bloquearPesquisaEditavel(Connection c, int id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT id_pesquisa FROM pesquisa WHERE id_pesquisa=? FOR UPDATE")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new ApiException(404, "Pesquisa não encontrada.");
            }
        }
        try (PreparedStatement s = c.prepareStatement("SELECT EXISTS(SELECT 1 FROM participacao WHERE id_pesquisa=?)")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                if (r.getBoolean(1)) throw new ApiException(409, "Uma pesquisa que já recebeu respostas não pode ser alterada.");
            }
        }
    }

    public int salvarPesquisa(Connection c, Integer id, PesquisaSalvarRequest dados) throws SQLException {
        String sql = id == null
            ? "INSERT INTO pesquisa(anonima,data_pesquisa,hora_ini,hora_fim,titulo,descricao) VALUES(?,?,?,?,?,?) RETURNING id_pesquisa"
            : "UPDATE pesquisa SET anonima=?,data_pesquisa=?,hora_ini=?,hora_fim=?,titulo=?,descricao=? WHERE id_pesquisa=? RETURNING id_pesquisa";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setBoolean(1, !Boolean.FALSE.equals(dados.anonima()));
            s.setObject(2, dados.dataPesquisa()); s.setObject(3, dados.horaIni()); s.setObject(4, dados.horaFim());
            s.setString(5, dados.titulo().trim()); s.setString(6, dados.descricao());
            if (id != null) s.setInt(7, id);
            try (ResultSet r = s.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    public void substituirQuestoes(Connection c, int id, List<Integer> questoes) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("DELETE FROM pesquisa_questao WHERE id_pesquisa=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
        try (PreparedStatement s = c.prepareStatement("INSERT INTO pesquisa_questao(id_pesquisa,id_questao,ordem_questao) VALUES(?,?,?)")) {
            for (int i = 0; i < questoes.size(); i++) {
                s.setInt(1, id); s.setInt(2, questoes.get(i)); s.setInt(3, i + 1); s.addBatch();
            }
            s.executeBatch();
        }
    }

    public void substituirGrupos(Connection c, int id, List<Integer> grupos) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("DELETE FROM grupo_pesquisa WHERE id_pesquisa=?")) {
            s.setInt(1, id); s.executeUpdate();
        }
        try (PreparedStatement s = c.prepareStatement("INSERT INTO grupo_pesquisa(id_pesquisa,id_grupo) VALUES(?,?)")) {
            for (int grupo : grupos) { s.setInt(1, id); s.setInt(2, grupo); s.addBatch(); }
            s.executeBatch();
        }
    }

    public void criarControlesDestinatarios(Connection c, int idPesquisa) throws SQLException {
        String sql = """
            INSERT INTO controle_participacao(id_usu,id_pesquisa,concluida)
            SELECT DISTINCT gu.id_usu, gp.id_pesquisa, FALSE
            FROM grupo_pesquisa gp JOIN grupo_usuario gu ON gu.id_grupo=gp.id_grupo
            WHERE gp.id_pesquisa=?
            ON CONFLICT(id_usu,id_pesquisa) DO NOTHING
            """;
        try (PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, idPesquisa); s.executeUpdate(); }
    }
}
