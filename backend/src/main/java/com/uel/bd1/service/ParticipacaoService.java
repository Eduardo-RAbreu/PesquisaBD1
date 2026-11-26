package com.uel.bd1.service;

import com.uel.bd1.dao.ParticipacaoDao;
import com.uel.bd1.dao.PesquisaDao;
import com.uel.bd1.model.EnvioRespostasRequest;
import com.uel.bd1.model.ControleParticipacao;
import com.uel.bd1.model.Participacao;
import com.uel.bd1.model.Pesquisa;
import com.uel.bd1.model.Resposta;
import com.uel.bd1.model.RespostaRequest;

import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

@Service
public class ParticipacaoService {

    private final DataSource dataSource;
    private final ParticipacaoDao participacaoDao;
    private final PesquisaDao pesquisaDao;

    public ParticipacaoService(
            DataSource dataSource,
            ParticipacaoDao participacaoDao,
            PesquisaDao pesquisaDao) {

        this.dataSource = dataSource;
        this.participacaoDao = participacaoDao;
        this.pesquisaDao = pesquisaDao;
    }

    public void enviar(int idUsuario, int idPesquisa, EnvioRespostasRequest dados)
            throws SQLException {
        validarEntrada(idUsuario, idPesquisa, dados);

        try (Connection conexao = dataSource.getConnection()) {
            conexao.setAutoCommit(false);

            try {
                Pesquisa pesquisa = pesquisaDao.listarID(conexao, idPesquisa);
                if (pesquisa == null) {
                    throw new IllegalArgumentException("Pesquisa não encontrada.");
                }

                List<Integer> grupos = participacaoDao.listarGruposAutorizados(
                    conexao, idUsuario, idPesquisa
                );
                int idGrupo = selecionarGrupo(grupos, dados.idGrupoRespondente());

                participacaoDao.criarControleSeAusente(conexao, idUsuario, idPesquisa);
                ControleParticipacao controle = participacaoDao.buscarControleParaAtualizacao(
                    conexao, idUsuario, idPesquisa
                );

                if (controle == null || controle.concluida()) {
                    throw new IllegalArgumentException(
                        "Você já respondeu a esta pesquisa ou não possui controle válido."
                    );
                }

                validarHorario(pesquisa);

                Map<Integer, String> tipos = pesquisaDao.listarTiposDasQuestoes(conexao, idPesquisa);
                validarRespostas(dados.respostas(), tipos);

                int idParticipacao = participacaoDao.inserirRetornandoId(
                    conexao, new Participacao(null, idPesquisa, idGrupo, null, null)
                );

                if (!Boolean.TRUE.equals(pesquisa.anonima())) {
                    participacaoDao.identificarParticipacao(conexao, idParticipacao, idUsuario);
                }

                for (RespostaRequest item : dados.respostas()) {
                    Resposta resposta = new Resposta(
                        null, idParticipacao, item.idQuestao(), item.texto(), item.valorEscala()
                    );

                    if (!participacaoDao.inserirResposta(conexao, resposta)) {
                        throw new SQLException("Não foi possível salvar uma resposta.");
                    }
                }

                if (!participacaoDao.marcarConcluida(conexao, idUsuario, idPesquisa)) {
                    throw new SQLException("Não foi possível concluir a participação.");
                }

                conexao.commit();
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                throw e;
            }
        }
    }

    private void validarEntrada(int idUsuario, int idPesquisa, EnvioRespostasRequest dados) {
        if (idUsuario <= 0 || idPesquisa <= 0) {
            throw new IllegalArgumentException("Informe um usuário e uma pesquisa válidos.");
        }
        if (dados == null || dados.respostas() == null || dados.respostas().isEmpty()) {
            throw new IllegalArgumentException("Informe as respostas da pesquisa.");
        }
        if (dados.idGrupoRespondente() != null && dados.idGrupoRespondente() <= 0) {
            throw new IllegalArgumentException("Informe um grupo respondente válido.");
        }
    }

    private int selecionarGrupo(List<Integer> grupos, Integer idGrupoInformado) {
        if (grupos.isEmpty()) {
            throw new IllegalArgumentException("Você não está autorizado a responder a esta pesquisa.");
        }
        if (idGrupoInformado != null) {
            if (!grupos.contains(idGrupoInformado)) {
                throw new IllegalArgumentException(
                    "O grupo informado não está autorizado para este usuário e pesquisa."
                );
            }
            return idGrupoInformado;
        }
        if (grupos.size() != 1) {
            throw new IllegalArgumentException(
                "Informe o grupo respondente, pois você pertence a vários grupos destinatários."
            );
        }
        return grupos.get(0);
    }

    private void validarHorario(Pesquisa pesquisa) {
        LocalDateTime agora = LocalDateTime.now(ZoneId.of("America/Sao_Paulo"));
        LocalDateTime inicio = LocalDateTime.of(pesquisa.dataPesquisa(), pesquisa.horaIni());
        LocalDateTime fim = LocalDateTime.of(pesquisa.dataPesquisa(), pesquisa.horaFim());

        if (agora.isBefore(inicio) || !agora.isBefore(fim)) {
            throw new IllegalArgumentException("A pesquisa está fora do período permitido para respostas.");
        }
    }

    private void validarRespostas(List<RespostaRequest> respostas, Map<Integer, String> tipos) {
        if (tipos.isEmpty()) {
            throw new IllegalArgumentException("A pesquisa não possui questões cadastradas.");
        }

        Set<Integer> respondidas = new HashSet<>();
        for (RespostaRequest resposta : respostas) {
            if (resposta == null || resposta.idQuestao() == null || resposta.idQuestao() <= 0) {
                throw new IllegalArgumentException("Informe uma questão válida em cada resposta.");
            }
            if (!respondidas.add(resposta.idQuestao())) {
                throw new IllegalArgumentException("Uma questão foi respondida mais de uma vez.");
            }

            String tipo = tipos.get(resposta.idQuestao());
            if (tipo == null) {
                throw new IllegalArgumentException("Uma das questões não pertence à pesquisa.");
            }

            if ("ESCALA".equals(tipo)) {
                if (resposta.texto() != null || resposta.valorEscala() == null
                        || resposta.valorEscala() < 1 || resposta.valorEscala() > 5) {
                    throw new IllegalArgumentException("Questões de escala exigem somente uma nota de 1 a 5.");
                }
            } else if ("TEXTO".equals(tipo)) {
                if (resposta.valorEscala() != null || resposta.texto() == null
                        || resposta.texto().isBlank()) {
                    throw new IllegalArgumentException("Questões de texto exigem somente um texto não vazio.");
                }
            } else {
                throw new IllegalArgumentException("Tipo de questão não suportado.");
            }
        }

        if (!respondidas.equals(tipos.keySet())) {
            throw new IllegalArgumentException("Responda todas as questões da pesquisa.");
        }
    }

}
