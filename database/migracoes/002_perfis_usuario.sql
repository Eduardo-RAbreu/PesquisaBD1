BEGIN;

ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS perfil varchar(20) NOT NULL DEFAULT 'USUARIO'
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('ADMIN', 'USUARIO'));

UPDATE usuario
SET perfil = 'ADMIN'
WHERE email = 'eduardo.uel@uel.br'
  AND NOT EXISTS (SELECT 1 FROM usuario WHERE perfil = 'ADMIN');

COMMIT;
