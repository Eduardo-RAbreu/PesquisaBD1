BEGIN;

CREATE TABLE IF NOT EXISTS participacao_identificada (
    id_part integer PRIMARY KEY REFERENCES participacao(id_part) ON DELETE CASCADE,
    id_usu integer NOT NULL REFERENCES usuario(id_usu) ON DELETE RESTRICT
);

COMMIT;
