package com.uel.bd1.controller;

import com.uel.bd1.model.*;
import com.uel.bd1.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {
    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/media-por-tema")
    public List<MediaPorTema> mediaPorTema(
            @RequestParam(name = "de", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(name = "ate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(name = "minRespostas", defaultValue = "1") int minimo) throws SQLException {
        return service.mediaPorTema(de, ate, minimo);
    }

    @GetMapping("/media-por-grupo")
    public List<MediaPorGrupo> mediaPorGrupo(
            @RequestParam(name = "idPesquisa", required = false) Integer idPesquisa) throws SQLException {
        return service.mediaPorGrupo(idPesquisa);
    }

    @GetMapping("/evolucao")
    public List<EvolucaoNoTempo> evolucao(
            @RequestParam(name = "idTema", required = false) Integer idTema) throws SQLException {
        return service.evolucao(idTema);
    }

    @GetMapping("/ranking-questoes")
    public List<RankingQuestao> ranking(
            @RequestParam(name = "ordem", defaultValue = "melhores") String ordem,
            @RequestParam(name = "busca", defaultValue = "") String busca,
            @RequestParam(name = "idsTemas", required = false) List<Integer> idsTemas,
            @RequestParam(name = "minRespostas", defaultValue = "1") int minimo,
            @RequestParam(name = "limite", defaultValue = "10") int limite) throws SQLException {
        return service.ranking(ordem, busca, idsTemas, minimo, limite);
    }

    @GetMapping("/adesao")
    public AdesaoPesquisa adesao(@RequestParam("idPesquisa") int idPesquisa) throws SQLException {
        return service.adesao(idPesquisa);
    }
}
