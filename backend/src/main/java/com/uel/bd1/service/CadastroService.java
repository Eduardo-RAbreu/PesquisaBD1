package com.uel.bd1.service;

import com.uel.bd1.dao.CadastroDao;
import com.uel.bd1.model.CadastroUsuarioRequest;
import com.uel.bd1.model.PesquisaSalvarRequest;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

@Service
public class CadastroService {
    private final DataSource dataSource;
    private final CadastroDao dao;

    public CadastroService(DataSource dataSource, CadastroDao dao) {
        this.dataSource = dataSource;
        this.dao = dao;
    }

    public int cadastrarUsuario(CadastroUsuarioRequest dados) throws SQLException {
        if (dados == null) throw new IllegalArgumentException("Informe os dados do usuário.");
        texto(dados.nome(), 120, "nome"); texto(dados.email(), 180, "e-mail");
        if (dados.senha() == null || dados.senha().isBlank() || dados.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Informe uma senha não vazia de até 72 bytes em UTF-8.");
        }
        List<Integer> grupos = ids(dados.idsGrupos(), false, "grupos");
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id = dao.inserirUsuario(c, dados);
                dao.adicionarGruposUsuario(c, id, grupos);
                c.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                desfazer(c, e); throw e;
            }
        }
    }

    public int salvarPesquisa(Integer id, PesquisaSalvarRequest dados) throws SQLException {
        if (id != null && id <= 0) throw new IllegalArgumentException("Informe um ID de pesquisa positivo.");
        if (dados == null) throw new IllegalArgumentException("Informe os dados da pesquisa.");
        texto(dados.titulo(), 180, "título");
        if (dados.dataPesquisa() == null || dados.horaIni() == null || dados.horaFim() == null
                || !dados.horaFim().isAfter(dados.horaIni())) {
            throw new IllegalArgumentException("Informe data e horários válidos; o fim deve ser posterior ao início.");
        }
        List<Integer> questoes = ids(dados.idsQuestoes(), true, "questões");
        List<Integer> grupos = ids(dados.idsGrupos(), true, "grupos");
        try (Connection c = dataSource.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (id != null) dao.bloquearPesquisaEditavel(c, id);
                int salvo = dao.salvarPesquisa(c, id, dados);
                dao.substituirQuestoes(c, salvo, questoes);
                dao.substituirGrupos(c, salvo, grupos);
                dao.criarControlesDestinatarios(c, salvo);
                c.commit();
                return salvo;
            } catch (SQLException | RuntimeException e) {
                desfazer(c, e); throw e;
            }
        }
    }

    private void texto(String valor, int limite, String nome) {
        if (valor == null || valor.isBlank() || valor.length() > limite) {
            throw new IllegalArgumentException("Informe " + nome + " não vazio, com até " + limite + " caracteres.");
        }
    }

    private List<Integer> ids(List<Integer> valores, boolean obrigatorio, String nome) {
        if (valores == null) valores = List.of();
        if (obrigatorio && valores.isEmpty()) throw new IllegalArgumentException("Selecione ao menos um item em " + nome + ".");
        Set<Integer> unicos = new HashSet<>();
        for (Integer id : valores) {
            if (id == null || id <= 0 || !unicos.add(id)) throw new IllegalArgumentException("Há IDs inválidos ou repetidos em " + nome + ".");
        }
        return valores;
    }

    private void desfazer(Connection c, Exception erro) {
        try { c.rollback(); } catch (SQLException rollback) { erro.addSuppressed(rollback); }
    }
}
