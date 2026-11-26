BEGIN;

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_perfil;
UPDATE usuario SET perfil = 'USUARIO';
ALTER TABLE usuario ALTER COLUMN perfil SET DEFAULT 'USUARIO';
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_perfil CHECK (perfil = 'USUARIO');

COMMIT;
