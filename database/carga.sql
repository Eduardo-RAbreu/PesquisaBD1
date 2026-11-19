BEGIN;

INSERT INTO tipo_questao (nome_tipo, descricao) VALUES
('ESCALA', 'Avaliação de 1 a 5'),
('TEXTO', 'Resposta aberta');

INSERT INTO tema (nome_tema, descricao) VALUES
('Infraestrutura', 'Instalações da universidade'),
('Atendimento', 'Qualidade do atendimento'),
('Ensino', 'Qualidade do ensino');

INSERT INTO grupo (nome_grupo) VALUES
('Estudantes'), ('Professores'), ('Funcionários');

INSERT INTO usuario (nome, email) VALUES
('Eduardo Abreu', 'eduardo.uel@uel.br'),
('Caio Rossate', 'CaioR.uel@uel.br'),
('Pedro Goes', 'pg.uel@uel.br'),
('Renan Jusan', 'RenanJusan.uel@uel.br'),
('Murilo Neves', 'muriloN.uel@uel.br'),
('Ana Luisa', 'Ana12.uel@uel.br'),
('Lucas Cabeca', 'lucascabeca.uel@uel.br'),
('Joao Carlos', 'JC.uel@uel.br'),
('Arthur Martins', 'arth010.uel@uel.br'),
('Frederico Bonim', 'terk.uel@uel.br'),
('Victor Morimitsu', 'mosivi04.uel@uel.br'),
('Murilo Fla', 'murilin.uel@uel.br'),
('Kenai Gemp', 'Kenai.uel@uel.br'),
('Anderson Silveira', 'Anderson.uel@uel.br'),
('Evandro Baccarin', 'evandro010.uel@uel.br'),
('Rodolfo Barros', 'Rodolfo.uel@uel.br'),
('Pedro Paulo', 'PP.uel@uel.br'),
('Carmen Lucia', 'CarmenL.uel@uel.br'),
('Jones Marcos', 'JonesM.uel@uel.br'),
('Paula Senise', 'Pseniseg.uel@uel.br');

INSERT INTO grupo_usuario (id_usu, id_grupo)
SELECT id_usu,
       CASE WHEN id_usu <= 13 THEN 1 WHEN id_usu <= 16 THEN 2 ELSE 3 END
FROM usuario;

INSERT INTO pesquisa
    (anonima, data_pesquisa, hora_ini, hora_fim, titulo, descricao)
VALUES
(TRUE, '2026-11-20', '08:00', '18:00',
 'Pesquisa de opinião', 'Avaliação de Filmes'),
(FALSE, '2026-11-21', '09:00', '17:00',
 'Pesquisa de opinião', 'Avaliação das comidas no RU'),
(TRUE, '2026-12-25', '08:00', '23:59',
 'Pesquisa de opinião', 'Gosta do Natal?'),
(TRUE, '2026-11-20', '08:00', '18:00',
 'Pesquisa da biblioteca', 'Avaliação da biblioteca'),
(FALSE, '2026-11-21', '09:00', '17:00',
 'Pesquisa de conceitos', 'Avaliação dos conceitos básicos na matemática');

INSERT INTO grupo_pesquisa (id_pesquisa, id_grupo)
SELECT p.id_pesquisa, g.id_grupo
FROM pesquisa p CROSS JOIN grupo g;

INSERT INTO questao (id_tema, id_tipo_questao, descricao) VALUES
(3, 1, 'De 1 a 5, quanto você gostou dos filmes apresentados?'),
(1, 1, 'De 1 a 5, como avalia a qualidade de imagem dos filmes?'),
(1, 1, 'De 1 a 5, como avalia a qualidade de som dos filmes?'),
(3, 1, 'De 1 a 5, quanto recomendaria os filmes?'),
(3, 2, 'Qual filme você mais gostou e por quê?'),
(3, 2, 'O que poderia melhorar na seleção de filmes?'),
(2, 1, 'De 1 a 5, como avalia o atendimento no restaurante universitário?'),
(1, 1, 'De 1 a 5, como avalia a limpeza do restaurante universitário?'),
(2, 1, 'De 1 a 5, como avalia o sabor das refeições?'),
(2, 1, 'De 1 a 5, como avalia a variedade do cardápio?'),
(2, 2, 'Qual prato do restaurante você mais gosta?'),
(2, 2, 'O que deveria melhorar no restaurante universitário?'),
(3, 1, 'De 1 a 5, quanto gosta das comemorações de Natal?'),
(2, 1, 'De 1 a 5, como avalia as atividades de Natal?'),
(1, 1, 'De 1 a 5, como avalia a decoração de Natal?'),
(3, 1, 'De 1 a 5, quanto gosta de reunir a família no Natal?'),
(3, 2, 'O que você mais gosta no Natal?'),
(3, 2, 'O que mudaria nas comemorações de Natal?'),
(1, 1, 'De 1 a 5, como avalia o espaço físico da biblioteca?'),
(2, 1, 'De 1 a 5, como avalia o atendimento da biblioteca?'),
(3, 1, 'De 1 a 5, como avalia o acervo da biblioteca?'),
(1, 1, 'De 1 a 5, como avalia os computadores da biblioteca?'),
(3, 2, 'Quais livros deveriam ser adquiridos pela biblioteca?'),
(1, 2, 'O que poderia melhorar na biblioteca?'),
(3, 1, 'De 1 a 5, como avalia seu conhecimento de álgebra básica?'),
(3, 1, 'De 1 a 5, como avalia seu conhecimento de geometria?'),
(3, 1, 'De 1 a 5, como avalia seu conhecimento de frações?'),
(3, 1, 'De 1 a 5, como avalia seu conhecimento de porcentagem?'),
(3, 2, 'Qual conceito de matemática considera mais difícil?'),
(3, 2, 'Que conteúdo de matemática gostaria de revisar?');

-- Cada bloco de seis questões foi atribuído à sua pesquisa.
INSERT INTO pesquisa_questao (id_pesquisa, id_questao, ordem_questao)
SELECT ((id_questao - 1) / 6) + 1,
       id_questao,
       ((id_questao - 1) % 6) + 1
FROM questao;

-- Habilita usuários para as pesquisas, mas ainda não os marca como concluintes.
-- Não há ligação entre este controle e os IDs de participação.
INSERT INTO controle_participacao (id_usu, id_pesquisa, concluida)
SELECT u.id_usu, p.id_pesquisa, FALSE
FROM usuario u CROSS JOIN pesquisa p;

-- Participações de teste fictícias, não vinculadas a pessoas identificadas.
-- Seis participações por pesquisa, duas em cada grupo (apenas para testes).
-- Horários individuais ficam NULL para evitar correlação direta por timestamp.
INSERT INTO participacao (id_pesquisa, id_grupo_respondente, data_ini, data_fim)
SELECT p.id_pesquisa, ((n.numero - 1) % 3) + 1, NULL, NULL
FROM pesquisa p CROSS JOIN generate_series(1, 6) AS n(numero);

-- Cada participação responde às seis questões de sua pesquisa.
-- Para as questões ESCALA: nota numérica entre 1 e 5.
-- Para as questões TEXTO: texto preenchido.
INSERT INTO resposta (id_part, id_questao, texto, valor_escala)
SELECT pa.id_part,
       pq.id_questao,
       CASE WHEN tq.nome_tipo = 'TEXTO'
            THEN 'Resposta de exemplo para a questão ' || pq.id_questao
            ELSE NULL END,
       CASE WHEN tq.nome_tipo = 'ESCALA'
            THEN (((pa.id_part + pq.id_questao) % 5) + 1)::smallint
            ELSE NULL END
FROM participacao pa
JOIN pesquisa_questao pq ON pq.id_pesquisa = pa.id_pesquisa
JOIN questao q ON q.id_questao = pq.id_questao
JOIN tipo_questao tq ON tq.id_tipo_questao = q.id_tipo_questao;

COMMIT;

-- Conferência:
SELECT 'usuarios' AS tabela, COUNT(*) AS total FROM usuario
UNION ALL SELECT 'grupos', COUNT(*) FROM grupo
UNION ALL SELECT 'pesquisas', COUNT(*) FROM pesquisa
UNION ALL SELECT 'questoes', COUNT(*) FROM questao
UNION ALL SELECT 'participacoes', COUNT(*) FROM participacao
UNION ALL SELECT 'respostas', COUNT(*) FROM resposta;
