BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuario (
    id_usu integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome varchar(120) NOT NULL,
    email varchar(180) NOT NULL UNIQUE,
    senha_hash varchar(255) NOT NULL,
    CONSTRAINT ck_usuario_nome CHECK (btrim(nome) <> ''),
    CONSTRAINT ck_usuario_senha CHECK (btrim(senha_hash) <> '')
);

CREATE TABLE grupo (
    id_grupo integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_grupo varchar(120) NOT NULL UNIQUE,
    CONSTRAINT ck_grupo_nome CHECK (btrim(nome_grupo) <> '')
);

CREATE TABLE grupo_usuario (
    id_usu integer NOT NULL,
    id_grupo integer NOT NULL,
    PRIMARY KEY (id_usu, id_grupo),
    FOREIGN KEY (id_usu) REFERENCES usuario(id_usu) ON DELETE CASCADE,
    FOREIGN KEY (id_grupo) REFERENCES grupo(id_grupo) ON DELETE CASCADE
);

CREATE TABLE pesquisa (
    id_pesquisa integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    anonima boolean NOT NULL DEFAULT true,
    data_pesquisa date NOT NULL,
    hora_ini time NOT NULL,
    hora_fim time NOT NULL,
    titulo varchar(180) NOT NULL,
    descricao text,
    CONSTRAINT ck_pesquisa_horario CHECK (hora_fim > hora_ini),
    CONSTRAINT ck_pesquisa_titulo CHECK (btrim(titulo) <> '')
);

CREATE TABLE grupo_pesquisa (
    id_pesquisa integer NOT NULL,
    id_grupo integer NOT NULL,
    PRIMARY KEY (id_pesquisa, id_grupo),
    FOREIGN KEY (id_pesquisa) REFERENCES pesquisa(id_pesquisa) ON DELETE CASCADE,
    FOREIGN KEY (id_grupo) REFERENCES grupo(id_grupo) ON DELETE CASCADE
);

CREATE TABLE tema (
    id_tema integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_tema varchar(120) NOT NULL UNIQUE,
    descricao text,
    CONSTRAINT ck_tema_nome CHECK (btrim(nome_tema) <> '')
);

CREATE TABLE tipo_questao (
    id_tipo_questao integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_tipo varchar(30) NOT NULL UNIQUE,
    descricao text,
    -- O modelo atual suporta os dois tipos pedidos no projeto.
    CONSTRAINT ck_tipo_questao CHECK (nome_tipo IN ('ESCALA', 'TEXTO'))
);

CREATE TABLE questao (
    id_questao integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tema integer NOT NULL,
    id_tipo_questao integer NOT NULL,
    descricao text NOT NULL,
    FOREIGN KEY (id_tema) REFERENCES tema(id_tema) ON DELETE RESTRICT,
    FOREIGN KEY (id_tipo_questao) REFERENCES tipo_questao(id_tipo_questao) ON DELETE RESTRICT,
    CONSTRAINT ck_questao_descricao CHECK (btrim(descricao) <> '')
);

CREATE TABLE pesquisa_questao (
    id_pesquisa integer NOT NULL,
    id_questao integer NOT NULL,
    ordem_questao integer NOT NULL,
    PRIMARY KEY (id_pesquisa, id_questao),
    UNIQUE (id_pesquisa, ordem_questao),
    FOREIGN KEY (id_pesquisa) REFERENCES pesquisa(id_pesquisa) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES questao(id_questao) ON DELETE RESTRICT,
    CONSTRAINT ck_ordem_questao CHECK (ordem_questao > 0)
);

CREATE TABLE controle_participacao (
    id_usu integer NOT NULL,
    id_pesquisa integer NOT NULL,
    concluida boolean NOT NULL DEFAULT false,
    PRIMARY KEY (id_usu, id_pesquisa),
    FOREIGN KEY (id_usu) REFERENCES usuario(id_usu) ON DELETE CASCADE,
    FOREIGN KEY (id_pesquisa) REFERENCES pesquisa(id_pesquisa) ON DELETE CASCADE
);

CREATE TABLE participacao (
    id_part integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pesquisa integer NOT NULL,
    id_grupo_respondente integer,
    data_ini timestamp,
    data_fim timestamp,
    FOREIGN KEY (id_pesquisa) REFERENCES pesquisa(id_pesquisa) ON DELETE RESTRICT,
    FOREIGN KEY (id_grupo_respondente) REFERENCES grupo(id_grupo) ON DELETE RESTRICT,
    CONSTRAINT ck_part_datas CHECK (
        data_ini IS NULL OR data_fim IS NULL OR data_fim >= data_ini
    )
);

CREATE TABLE resposta (
    id_resposta integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_part integer NOT NULL,
    id_questao integer NOT NULL,
    texto text,
    valor_escala smallint,
    UNIQUE (id_part, id_questao),
    FOREIGN KEY (id_part) REFERENCES participacao(id_part) ON DELETE CASCADE,
    FOREIGN KEY (id_questao) REFERENCES questao(id_questao) ON DELETE RESTRICT,
    CONSTRAINT ck_resposta_apenas_um_tipo CHECK (
        (texto IS NOT NULL AND valor_escala IS NULL AND btrim(texto) <> '')
        OR (texto IS NULL AND valor_escala IS NOT NULL)
    ),
    CONSTRAINT ck_resposta_escala CHECK (valor_escala BETWEEN 1 AND 5)
);

CREATE INDEX idx_participacao_pesquisa ON participacao(id_pesquisa);
CREATE INDEX idx_resposta_questao ON resposta(id_questao);
CREATE INDEX idx_questao_tema ON questao(id_tema);

COMMIT;
