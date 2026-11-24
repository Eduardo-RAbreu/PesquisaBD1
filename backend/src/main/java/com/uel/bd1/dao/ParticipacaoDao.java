package com.uel.bd1.dao;

import com.uel.bd1.model.Participacao;
import com.uel.bd1.model.Resposta;
import com.uel.bd1.model.ControleParticipacao;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ParticipacaoDao {

    private final DataSource dataSource;

    public ParticipacaoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Participacao> listar() throws SQLException {
        List<Participacao> registros = new ArrayList<>();
        String sql = """
            SELECT id_part, id_pesquisa, id_grupo_respondente, data_ini, data_fim
            FROM participacao
            ORDER BY id_part
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new Participacao(
                        resultado.getInt("id_part"),
                        resultado.getInt("id_pesquisa"),
                        resultado.getObject("id_grupo_respondente", Integer.class),
                        resultado.getObject("data_ini", LocalDateTime.class),
                        resultado.getObject("data_fim", LocalDateTime.class)
                    ));
            }
        }
        return registros;
    }

    public Participacao listarID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_part, id_pesquisa, id_grupo_respondente, data_ini, data_fim
            FROM participacao
            WHERE id_part = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Participacao(
                        resultado.getInt("id_part"),
                        resultado.getInt("id_pesquisa"),
                        resultado.getObject("id_grupo_respondente", Integer.class),
                        resultado.getObject("data_ini", LocalDateTime.class),
                        resultado.getObject("data_fim", LocalDateTime.class)
                    );
                }
            }
        }
        return null;
    }

    public boolean inserir(Participacao dados) throws SQLException {
        validarParticipacao(dados);

        String sql = """
            INSERT INTO participacao (id_pesquisa, id_grupo_respondente, data_ini, data_fim)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idPesquisa());
            comando.setObject(2, dados.idGrupoRespondente(), Types.INTEGER);
            comando.setObject(3, dados.dataIni());
            comando.setObject(4, dados.dataFim());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um registro com esses dados únicos.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A pesquisa ou o grupo informado não existe.", e);
            }
            throw e;
        }
    }

    public boolean atualizar(Participacao dados) throws SQLException {
        validarParticipacao(dados);

        if (dados.idPart() == null || dados.idPart() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE participacao
            SET id_pesquisa = ?, id_grupo_respondente = ?, data_ini = ?, data_fim = ?
            WHERE id_part = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idPesquisa());
            comando.setObject(2, dados.idGrupoRespondente(), Types.INTEGER);
            comando.setObject(3, dados.dataIni());
            comando.setObject(4, dados.dataFim());
            comando.setInt(5, dados.idPart());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um registro com esses dados únicos.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A pesquisa ou o grupo informado não existe.", e);
            }
            throw e;
        }
    }

    public boolean deletar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM participacao WHERE id_part = ?";
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
                throw new IllegalArgumentException("A pesquisa ou o grupo informado não existe.", e);
            }
            throw e;
        }
    }

    private void validarParticipacao(Participacao dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de Participacao são obrigatórios.");
        }

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de pesquisa válido.");
        }

        if (dados.idGrupoRespondente() != null && dados.idGrupoRespondente() <= 0) {
            throw new IllegalArgumentException("Informe um grupo válido ou deixe-o nulo.");
        }

        if (dados.dataIni() != null && dados.dataFim() != null && dados.dataFim().isBefore(dados.dataIni())) {
            throw new IllegalArgumentException("O fim da participação não pode ser anterior ao início.");
        }
    }

    // CRUD das respostas; validações entre pesquisa e tipo serão feitas no Service.
    public List<Resposta> listarResposta() throws SQLException {
        List<Resposta> registros = new ArrayList<>();
        String sql = """
            SELECT id_resposta, id_part, id_questao, texto, valor_escala
            FROM resposta
            ORDER BY id_resposta
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new Resposta(
                        resultado.getInt("id_resposta"),
                        resultado.getInt("id_part"),
                        resultado.getInt("id_questao"),
                        resultado.getString("texto"),
                        resultado.getObject("valor_escala", Integer.class)
                    ));
            }
        }
        return registros;
    }

    public Resposta listarRespostaID(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_resposta, id_part, id_questao, texto, valor_escala
            FROM resposta
            WHERE id_resposta = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Resposta(
                        resultado.getInt("id_resposta"),
                        resultado.getInt("id_part"),
                        resultado.getInt("id_questao"),
                        resultado.getString("texto"),
                        resultado.getObject("valor_escala", Integer.class)
                    );
                }
            }
        }
        return null;
    }

    public boolean inserirResposta(Resposta dados) throws SQLException {
        validarResposta(dados);

        String sql = """
            INSERT INTO resposta (id_part, id_questao, texto, valor_escala)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idPart());
            comando.setInt(2, dados.idQuestao());
            comando.setString(3, dados.texto());
            comando.setObject(4, dados.valorEscala(), Types.SMALLINT);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Esta participação já possui uma resposta para essa questão.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A participação ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean atualizarResposta(Resposta dados) throws SQLException {
        validarResposta(dados);

        if (dados.idResposta() == null || dados.idResposta() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE resposta
            SET id_part = ?, id_questao = ?, texto = ?, valor_escala = ?
            WHERE id_resposta = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idPart());
            comando.setInt(2, dados.idQuestao());
            comando.setString(3, dados.texto());
            comando.setObject(4, dados.valorEscala(), Types.SMALLINT);
            comando.setInt(5, dados.idResposta());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Esta participação já possui uma resposta para essa questão.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A participação ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean deletarResposta(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM resposta WHERE id_resposta = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Esta participação já possui uma resposta para essa questão.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("A participação ou a questão informada não existe.", e);
            }
            throw e;
        }
    }

    private void validarResposta(Resposta dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de Resposta são obrigatórios.");
        }

        if (dados.idPart() == null || dados.idPart() <= 0) {
            throw new IllegalArgumentException("Informe um ID de participação válido.");
        }

        if (dados.idQuestao() == null || dados.idQuestao() <= 0) {
            throw new IllegalArgumentException("Informe um ID de questão válido.");
        }

        if ((dados.texto() == null) == (dados.valorEscala() == null)) {
            throw new IllegalArgumentException("Informe somente texto ou somente valor de escala.");
        }

        if (dados.texto() != null && dados.texto().isBlank()) {
            throw new IllegalArgumentException("O texto da resposta não pode ser vazio.");
        }

        if (dados.valorEscala() != null && (dados.valorEscala() < 1 || dados.valorEscala() > 5)) {
            throw new IllegalArgumentException("O valor da escala deve estar entre 1 e 5.");
        }
    }

    // CRUD do controle individual, separado dos dados das respostas anônimas.
    public List<ControleParticipacao> listarControle() throws SQLException {
        List<ControleParticipacao> registros = new ArrayList<>();
        String sql = """
            SELECT id_usu, id_pesquisa, concluida
            FROM controle_participacao
            ORDER BY id_usu, id_pesquisa
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
                registros.add(new ControleParticipacao(
                        resultado.getInt("id_usu"),
                        resultado.getInt("id_pesquisa"),
                        resultado.getBoolean("concluida")
                    ));
            }
        }
        return registros;
    }

    public ControleParticipacao listarControleID(int idUsu, int idPesquisa) throws SQLException {
        if (idUsu <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        if (idPesquisa <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = """
            SELECT id_usu, id_pesquisa, concluida
            FROM controle_participacao
            WHERE id_usu = ? AND id_pesquisa = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idUsu);
            comando.setInt(2, idPesquisa);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new ControleParticipacao(
                        resultado.getInt("id_usu"),
                        resultado.getInt("id_pesquisa"),
                        resultado.getBoolean("concluida")
                    );
                }
            }
        }
        return null;
    }

    public boolean inserirControle(ControleParticipacao dados) throws SQLException {
        validarControleParticipacao(dados);

        String sql = """
            INSERT INTO controle_participacao (id_usu, id_pesquisa, concluida)
            VALUES (?, ?, ?)
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, dados.idUsu());
            comando.setInt(2, dados.idPesquisa());
            comando.setBoolean(3, dados.concluida());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um controle para esse usuário e pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("O usuário ou a pesquisa informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean atualizarControle(ControleParticipacao dados) throws SQLException {
        validarControleParticipacao(dados);

        if (dados.idUsu() == null || dados.idUsu() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de registro válido.");
        }

        String sql = """
            UPDATE controle_participacao
            SET concluida = ?
            WHERE id_usu = ? AND id_pesquisa = ?
            """;

        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setBoolean(1, dados.concluida());
            comando.setInt(2, dados.idUsu());
            comando.setInt(3, dados.idPesquisa());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um controle para esse usuário e pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("O usuário ou a pesquisa informada não existe.", e);
            }
            throw e;
        }
    }

    public boolean deletarControle(int idUsu, int idPesquisa) throws SQLException {
        if (idUsu <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        if (idPesquisa <= 0) {
            throw new IllegalArgumentException("Informe IDs positivos para a consulta ou exclusão.");
        }

        String sql = "DELETE FROM controle_participacao WHERE id_usu = ? AND id_pesquisa = ?";
        try (
            Connection conexao = dataSource.getConnection();
            PreparedStatement comando = conexao.prepareStatement(sql)
        ) {
            comando.setInt(1, idUsu);
            comando.setInt(2, idPesquisa);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe um controle para esse usuário e pesquisa.", e);
            }
            if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                throw new IllegalArgumentException("O usuário ou a pesquisa informada não existe.", e);
            }
            throw e;
        }
    }

    private void validarControleParticipacao(ControleParticipacao dados) {
        if (dados == null) {
            throw new IllegalArgumentException("Os dados de ControleParticipacao são obrigatórios.");
        }

        if (dados.idUsu() == null || dados.idUsu() <= 0) {
            throw new IllegalArgumentException("Informe um ID de usuário válido.");
        }

        if (dados.idPesquisa() == null || dados.idPesquisa() <= 0) {
            throw new IllegalArgumentException("Informe um ID de pesquisa válido.");
        }
    }
}
