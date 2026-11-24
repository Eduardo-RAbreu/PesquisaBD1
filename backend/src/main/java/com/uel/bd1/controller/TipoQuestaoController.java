package com.uel.bd1.controller;

import com.uel.bd1.dao.TipoQuestaoDao;
import com.uel.bd1.model.TipoQuestao;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController 
@RequestMapping("api/tipos")
public class TipoQuestaoController {
    private final TipoQuestaoDao tqDao;

    public TipoQuestaoController(TipoQuestaoDao tq){
        this.tqDao=tq;
    }
    @GetMapping 
    public List<TipoQuestao> listar() throws SQLException{
        return tqDao.listar();
    }
}
    