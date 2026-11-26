package com.uel.bd1.model;

import java.util.List;

public record AdesaoPesquisa(List<ParticipacaoPorGrupo> grupos, List<UsuarioPendente> pendentes) {}
