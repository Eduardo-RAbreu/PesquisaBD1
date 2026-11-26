package com.uel.bd1.service;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.dao.RelatorioDao;
import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.*;
import org.springframework.stereotype.Service;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class RelatorioService {
    private final RelatorioDao dao;
    private final PesquisaDao pesquisaDao;

    public RelatorioService(RelatorioDao dao, PesquisaDao pesquisaDao) {
        this.dao = dao;
        this.pesquisaDao = pesquisaDao;
    }

    public List<MediaPorTema> mediaPorTema() throws SQLException {
        return mediaPorTema(null, null, 1);
    }

    public List<MediaPorTema> mediaPorTema(LocalDate de, LocalDate ate, int minimo) throws SQLException {
        positivo(minimo, "mínimo de respostas");
        if (de != null && ate != null && de.isAfter(ate)) {
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à final.");
        }
        return dao.mediaPorTema(de, ate, minimo);
    }

    public List<MediaPorGrupo> mediaPorGrupo(Integer idPesquisa) throws SQLException {
        if (idPesquisa != null) conferirPesquisa(idPesquisa);
        return dao.mediaPorGrupo(idPesquisa);
    }

    public List<EvolucaoNoTempo> evolucao(Integer idTema) throws SQLException {
        if (idTema != null) positivo(idTema, "ID de tema");
        return dao.evolucao(idTema);
    }

    public List<RankingQuestao> ranking(String ordem, String busca, List<Integer> idsTemas,
            int minimo, int limite) throws SQLException {
        if (!"melhores".equals(ordem) && !"piores".equals(ordem)) {
            throw new IllegalArgumentException("Escolha melhores ou piores para a ordem.");
        }
        positivo(minimo, "mínimo de respostas");
        if (limite < 1 || limite > 50) throw new IllegalArgumentException("O limite deve estar entre 1 e 50.");
        List<Integer> temas = idsTemas == null ? List.of() : idsTemas;
        for (Integer id : temas) {
            if (id == null) throw new IllegalArgumentException("Informe IDs de temas positivos.");
            positivo(id, "ID de tema");
        }
        return dao.ranking("melhores".equals(ordem), busca == null ? "" : busca.trim(),
            temas.stream().distinct().toList(), minimo, limite);
    }

    public AdesaoPesquisa adesao(int idPesquisa) throws SQLException {
        conferirPesquisa(idPesquisa);
        return dao.adesao(idPesquisa);
    }

    private void conferirPesquisa(int idPesquisa) throws SQLException {
        positivo(idPesquisa, "ID de pesquisa");
        if (pesquisaDao.listarID(idPesquisa) == null) throw new ApiException(404, "Pesquisa não encontrada.");
    }

    private void positivo(int valor, String campo) {
        if (valor <= 0) throw new IllegalArgumentException("Informe um " + campo + " positivo.");
    }
}
