package com.uel.bd1.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.erro.ApiException;
import com.uel.bd1.model.Pesquisa;
import com.uel.bd1.model.PesquisaDados;
import com.uel.bd1.model.PesquisaRequest;

@RestController
@RequestMapping("/api/pesquisas")
public class PesquisaController {

    private final PesquisaDao pesquisaDao;

    public PesquisaController(PesquisaDao pesquisaDao) {
        this.pesquisaDao = pesquisaDao;
    }

    @GetMapping
    public List<Pesquisa> listar() {
        return pesquisaDao.listar();
    }

    @GetMapping("/{id}")
    public Pesquisa buscar(@PathVariable int id) {
        return buscarOuFalhar(id).comQuestoes(pesquisaDao.idsQuestoes(id));
    }

    @PostMapping
    public ResponseEntity<Map<String, Integer>> criar(@RequestBody PesquisaRequest req) {
        PesquisaDados dados = converter(req);
        try {
            int id = pesquisaDao.inserir(dados, req.idsQuestoes());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Alguma das questões escolhidas não existe.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable int id, @RequestBody PesquisaRequest req) {
        buscarOuFalhar(id);
        PesquisaDados dados = converter(req);

        boolean mudouQuestoes = !new HashSet<>(pesquisaDao.idsQuestoes(id))
                .equals(new HashSet<>(req.idsQuestoes()));
        if (mudouQuestoes && pesquisaDao.temParticipacoes(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta pesquisa já tem respostas, então não é possível trocar as questões dela.");
        }
        try {
            pesquisaDao.atualizar(id, dados, req.idsQuestoes());
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Alguma das questões escolhidas não existe.");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        try {
            pesquisaDao.excluir(id);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta pesquisa já tem participações e não pode ser excluída.");
        }
        return ResponseEntity.noContent().build();
    }


    private Pesquisa buscarOuFalhar(int id) {
        return pesquisaDao.buscarPorId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pesquisa não encontrada."));
    }

    // converte data e horas de texto
    private PesquisaDados converter(PesquisaRequest r) {
        if (r.titulo() == null || r.titulo().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escreva o título da pesquisa.");
        }

        LocalDate data;
        LocalTime ini;
        LocalTime fim;
        try {
            data = LocalDate.parse(r.dataPesquisa());
            ini = LocalTime.parse(r.horaIni());
            fim = LocalTime.parse(r.horaFim());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe a data e os horários da pesquisa.");
        }
        if (!fim.isAfter(ini)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O horário de fim precisa ser depois do início.");
        }
        if (r.idsQuestoes() == null || r.idsQuestoes().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha pelo menos uma questão para a pesquisa.");
        }

        String descricao = (r.descricao() == null || r.descricao().isBlank())
                ? null : r.descricao().trim();
        return new PesquisaDados(r.titulo().trim(), descricao, data, ini, fim, r.anonima());
    }
}
