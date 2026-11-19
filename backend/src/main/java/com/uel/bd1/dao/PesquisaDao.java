package com.uel.bd1.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.uel.bd1.model.Pesquisa;
import com.uel.bd1.model.PesquisaDados;

@Repository
public class PesquisaDao {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    // pesquisa recém-criada
    private static final String SELECT_BASE = """
            SELECT p.id_pesquisa, p.titulo, p.descricao, p.data_pesquisa,
                   p.hora_ini, p.hora_fim, p.anonima,
                   COUNT(pq.id_questao) AS total_questoes
              FROM pesquisa p
              LEFT JOIN pesquisa_questao pq ON pq.id_pesquisa = p.id_pesquisa
            """;

    private static final RowMapper<Pesquisa> MAPPER = (rs, i) -> new Pesquisa(
            rs.getInt("id_pesquisa"),
            rs.getString("titulo"),
            rs.getString("descricao"),
            rs.getObject("data_pesquisa", LocalDate.class).toString(),
            rs.getObject("hora_ini", LocalTime.class).format(HORA),
            rs.getObject("hora_fim", LocalTime.class).format(HORA),
            rs.getBoolean("anonima"),
            rs.getInt("total_questoes"),
            List.of());

    private final JdbcTemplate jdbc;

    public PesquisaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Pesquisa> listar() {
        return jdbc.query(
                SELECT_BASE + " GROUP BY p.id_pesquisa ORDER BY p.data_pesquisa DESC, p.hora_ini DESC",
                MAPPER);
    }

    public Optional<Pesquisa> buscarPorId(int id) {
        return jdbc.query(SELECT_BASE + " WHERE p.id_pesquisa = ? GROUP BY p.id_pesquisa", MAPPER, id)
                .stream().findFirst();
    }

    public List<Integer> idsQuestoes(int idPesquisa) {
        return jdbc.queryForList(
                "SELECT id_questao FROM pesquisa_questao WHERE id_pesquisa = ? ORDER BY ordem_questao",
                Integer.class, idPesquisa);
    }

    // verifica se a pesquisa tem partipações
    public boolean temParticipacoes(int idPesquisa) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM participacao WHERE id_pesquisa = ?", Integer.class, idPesquisa);
        return total != null && total > 0;
    }

    // Grava a pesquisa e as questões escolhidas
    
    @Transactional
    public int inserir(PesquisaDados d, List<Integer> idsQuestoes) {
        Integer id = jdbc.queryForObject("""
                INSERT INTO pesquisa (titulo, descricao, data_pesquisa, hora_ini, hora_fim, anonima)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_pesquisa
                """,
                Integer.class,
                d.titulo(), d.descricao(), d.dataPesquisa(), d.horaIni(), d.horaFim(), d.anonima());
        vincularQuestoes(id, idsQuestoes);
        return id;
    }

    @Transactional
    public void atualizar(int id, PesquisaDados d, List<Integer> idsQuestoes) {
        jdbc.update("""
                UPDATE pesquisa
                   SET titulo = ?, descricao = ?, data_pesquisa = ?, hora_ini = ?, hora_fim = ?, anonima = ?
                 WHERE id_pesquisa = ?
                """,
                d.titulo(), d.descricao(), d.dataPesquisa(), d.horaIni(), d.horaFim(), d.anonima(), id);

        // Só troca as questões se a escolha mudou.
        List<Integer> atuais = idsQuestoes(id);
        if (!new HashSet<>(atuais).equals(new HashSet<>(idsQuestoes))) {
            jdbc.update("DELETE FROM pesquisa_questao WHERE id_pesquisa = ?", id);
            vincularQuestoes(id, idsQuestoes);
        }
    }

    @Transactional
    public void excluir(int id) {
        jdbc.update("DELETE FROM grupo_pesquisa WHERE id_pesquisa = ?", id);
        jdbc.update("DELETE FROM pesquisa_questao WHERE id_pesquisa = ?", id);
        jdbc.update("DELETE FROM pesquisa WHERE id_pesquisa = ?", id);
    }

    private void vincularQuestoes(int idPesquisa, List<Integer> idsQuestoes) {
        int ordem = 1;
        for (Integer idQuestao : idsQuestoes) {
            jdbc.update(
                    "INSERT INTO pesquisa_questao (id_questao, id_pesquisa, ordem_questao) VALUES (?, ?, ?)",
                    idQuestao, idPesquisa, ordem++);
        }
    }
}
