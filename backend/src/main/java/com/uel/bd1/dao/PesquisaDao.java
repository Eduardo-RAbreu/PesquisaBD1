package com.uel.bd1.dao;

import com.uel.bd1.model.Grupo;
import com.uel.bd1.model.Pesquisa;
import com.uel.bd1.model.PesquisaQuestao;
import com.uel.bd1.model.Questao;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class PesquisaDao {

    private final DataSource dataSource;

    public PesquisaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Pesquisa listarID(Connection conexao, int idPesquisa) throws SQLException {
        String sql = """
            SELECT id_pesquisa, anonima, data_pesquisa, hora_ini, hora_fim, titulo, descricao
            FROM pesquisa
            WHERE id_pesquisa = ?
            FOR SHARE
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idPesquisa);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Pesquisa(
                        resultado.getInt("id_pesquisa"),
                        resultado.getBoolean("anonima"),
                        resultado.getObject("data_pesquisa", LocalDate.class),
                        resultado.getObject("hora_ini", LocalTime.class),
                        resultado.getObject("hora_fim", LocalTime.class),
                        resultado.getString("titulo"),
                        resultado.getString("descricao")
                    );
                }
            }
        }
        return null;
    }

    public Map<Integer, String> listarTiposDasQuestoes(Connection conexao, int idPesquisa)
            throws SQLException {
        Map<Integer, String> tipos = new LinkedHashMap<>();
        String sql = """
            SELECT q.id_questao, tq.nome_tipo
            FROM pesquisa_questao pq
            JOIN questao q ON q.id_questao = pq.id_questao
            JOIN tipo_questao tq ON tq.id_tipo_questao = q.id_tipo_questao
            WHERE pq.id_pesquisa = ?
            ORDER BY pq.ordem_questao
            FOR SHARE OF pq, q, tq
            """;

        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, idPesquisa);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    tipos.put(resultado.getInt("id_questao"), resultado.getString("nome_tipo"));
                }
            }
        }
        return tipos;
    }

    public List<Pesquisa> listar() throws SQLException {
        List<Pesquisa> registros = new ArrayList<>();
        String sql = """
            SELECT id_pesquisa, anonima, data_pesquisa, hora_ini, hora_fim, titulo, descricao
            FROM pesquisa
            ORDER BY id_pesquisa
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new Pesquisa(
                        resultado.getInt("id_pesquisa"),
                        resultado.getBoolean("anonima"),
                        resultado.getObject("data_pesquisa", LocalDate.class),
                        resultado.getObject("hora_ini", LocalTime.class),
                        resultado.getObject("hora_fim", LocalTime.class),
                        resultado.getString("titulo"),
                        resultado.getString("descricao")
                    ));
            }
        }
        return registros;
    }

    public Pesquisa listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_pesquisa, anonima, data_pesquisa, hora_ini, hora_fim, titulo, descricao
            FROM pesquisa
            WHERE id_pesquisa = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Pesquisa(
                        resultado.getInt("id_pesquisa"),
                        resultado.getBoolean("anonima"),
                        resultado.getObject("data_pesquisa", LocalDate.class),
                        resultado.getObject("hora_ini", LocalTime.class),
                        resultado.getObject("hora_fim", LocalTime.class),
                        resultado.getString("titulo"),
                        resultado.getString("descricao")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(Pesquisa dados) throws SQLException {
        validarPesquisa(dados);

        String sql = """
            INSERT INTO pesquisa (anonima, data_pesquisa, hora_ini, hora_fim, titulo, descricao)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setBoolean(1, dados.anonima() == null || dados.anonima());
            comando.setObject(2, dados.dataPesquisa());
            comando.setObject(3, dados.horaIni());
            comando.setObject(4, dados.horaFim());
            comando.setString(5, dados.titulo());
            comando.setString(6, dados.descricao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um registro com esses dados únicos.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir a pesquisa: existem participações vinculadas.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(Pesquisa dados) throws SQLException {
        validarPesquisa(dados);

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE pesquisa
            SET anonima = ?, data_pesquisa = ?, hora_ini = ?, hora_fim = ?, titulo = ?, descricao = ?
            WHERE id_pesquisa = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setBoolean(1, dados.anonima() == null || dados.anonima());
            comando.setObject(2, dados.dataPesquisa());
            comando.setObject(3, dados.horaIni());
            comando.setObject(4, dados.horaFim());
            comando.setString(5, dados.titulo());
            comando.setString(6, dados.descricao());
            comando.setInt(7, dados.idPesquisa());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um registro com esses dados únicos.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir a pesquisa: existem participações vinculadas.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM pesquisa WHERE id_pesquisa = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um registro com esses dados únicos.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Não é possível excluir a pesquisa: existem participações vinculadas.", e);
            }
            throw e;
        }
    }

    private void validarPesquisa(Pesquisa dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de Pesquisa são obrigatórios.");
        }

        if (dados.titulo() == null || dados.titulo().isBlank() || dados.titulo().length() > 180) {
            throw new IllegalArgumentException("Informe um título não vazio, com até 180 caracteres.");
        }

        if (dados.dataPesquisa() == null || dados.horaIni() == null || dados.horaFim() == null) {
            throw new IllegalArgumentException("Informe data e horários da pesquisa.");
        }

        if (!dados.horaFim().isAfter(dados.horaIni())) {
            throw new IllegalArgumentException("O horário final deve ser posterior ao inicial.");
        }
    }

    public List<PesquisaQuestao> listarQuestao() throws SQLException {
        List<PesquisaQuestao> registros = new ArrayList<>();
        String sql = """
            SELECT id_pesquisa, id_questao, ordem_questao
            FROM pesquisa_questao
            ORDER BY id_pesquisa, id_questao
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new PesquisaQuestao(
                        resultado.getInt("id_pesquisa"),
                        resultado.getInt("id_questao"),
                        resultado.getInt("ordem_questao")
                    ));
            }
        }
        return registros;
    }

    public PesquisaQuestao listarQuestaoID(int idPesquisa, int idQuestao) throws SQLException {
        if (idPesquisa <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        if (idQuestao <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_pesquisa, id_questao, ordem_questao
            FROM pesquisa_questao
            WHERE id_pesquisa = ? AND id_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idPesquisa);
            comando.setInt(2, idQuestao);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new PesquisaQuestao(
                        resultado.getInt("id_pesquisa"),
                        resultado.getInt("id_questao"),
                        resultado.getInt("ordem_questao")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserirQuestao(PesquisaQuestao dados) throws SQLException {
        validarPesquisaQuestao(dados);

        String sql = """
            INSERT INTO pesquisa_questao (id_pesquisa, id_questao, ordem_questao)
            VALUES (?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idPesquisa());
            comando.setInt(2, dados.idQuestao());
            comando.setInt(3, dados.ordemQuestao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A questão ou a posição já está cadastrada nesta pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A pesquisa ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean atualizarQuestao(PesquisaQuestao dados) throws SQLException {
        validarPesquisaQuestao(dados);

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        if (dados.idQuestao() == null || dados.idQuestao() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE pesquisa_questao
            SET ordem_questao = ?
            WHERE id_pesquisa = ? AND id_questao = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.ordemQuestao());
            comando.setInt(2, dados.idPesquisa());
            comando.setInt(3, dados.idQuestao());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A questão ou a posição já está cadastrada nesta pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A pesquisa ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean deletarQuestao(int idPesquisa, int idQuestao) throws SQLException {
        if (idPesquisa <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        if (idQuestao <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM pesquisa_questao WHERE id_pesquisa = ? AND id_questao = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idPesquisa);
            comando.setInt(2, idQuestao);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A questão ou a posição já está cadastrada nesta pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A pesquisa ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    private void validarPesquisaQuestao(PesquisaQuestao dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de PesquisaQuestao são obrigatórios.");
        }

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de pesquisa válido.");
        }

        if (dados.idQuestao() == null || dados.idQuestao() <= 0) {
            throw new IllegalArgumentException("Informe um ID de questão válido.");
        }

        if (dados.ordemQuestao() == null || dados.ordemQuestao() <= 0) {
            throw new IllegalArgumentException("A ordem da questão deve ser positiva.");
        }
    }


    public boolean adicionarGrupo(int idGrupo, int idPesquisa) throws SQLException{
        if(idGrupo <= 0  || idPesquisa <=0){
            throw new IllegalArgumentException("Informe IDs positivos para a consulta.");
        }

        String sql ="""
                Insert into grupo_pesquisa(id_grupo,id_pesquisa)
                values(?, ?)
                """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idGrupo);
            comando.setInt(2, idPesquisa);
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

    public boolean removerGrupo(int idGrupo, int idPesquisa) throws SQLException{
        if(idGrupo <= 0  || idPesquisa <=0){
            throw new IllegalArgumentException("Informe IDs positivos para a consulta.");
        }

        String sql ="""
                Delete from grupo_pesquisa
                where id_grupo = ? and id_pesquisa = ?
                """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idGrupo);
            comando.setInt(2, idPesquisa);
            return comando.executeUpdate() > 0;
        }
    }

    public List<Grupo> listarGrupos(int idPesquisa) throws SQLException {

        if (idPesquisa <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de Pesquisa positivo."
            );
        }

        List<Grupo> registros = new ArrayList<>();

        String sql = """
            SELECT g.id_grupo as idgrupo, g.nome_grupo as nome_grupo
            FROM grupo_pesquisa p
            JOIN grupo g ON g.id_grupo = p.id_grupo
            WHERE p.id_pesquisa = ?
            ORDER BY g.nome_grupo, g.id_grupo
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idPesquisa);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                        registros.add(new Grupo(resultado.getInt("idgrupo"), resultado.getString("nome_grupo")));
                }
            }
        }

        return registros;
    }

    public List<Questao> listarQuestoesDaPesquisa(int idPesquisa) throws SQLException {
        if (idPesquisa <= 0) {
            throw new IllegalArgumentException(
                "Informe um ID de Pesquisa positivo."
            );
        }

        List<Questao> registros = new ArrayList<>();

        String sql = """
                Select
                q.id_questao as id_questao,
                q.id_tema as id_tema,
                q.id_tipo_questao as id_tipo_questao,
                q.descricao as descricao
                from pesquisa_questao p
                join questao q on q.id_questao = p.id_questao
                where p.id_pesquisa = ?
                ORDER BY p.ordem_questao
                """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idPesquisa);

            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                        registros.add(new Questao(resultado.getInt("id_questao"), resultado.getInt("id_tema"), resultado.getInt("id_tipo_questao"), resultado.getString("descricao")));
                }
            }
        }

        return registros;
    }
}
