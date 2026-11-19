package com.uel.bd1.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uel.bd1.dao.TipoQuestaoDao;
import com.uel.bd1.model.TipoQuestao;

@RestController
@RequestMapping("/api/tipos-questao")
public class TipoQuestaoController {

    private final TipoQuestaoDao tipoDao;

    public TipoQuestaoController(TipoQuestaoDao tipoDao) {
        this.tipoDao = tipoDao;
    }

    @GetMapping
    public List<TipoQuestao> listar() {
        return tipoDao.listar();
    }
}
