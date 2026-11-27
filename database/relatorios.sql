
Select
    t.id_tema,
    t.nome_tema,
    coalesce(round(avg(r.valor_escala), 2), 0) as media,
    count(r.id_resposta) as total_respostas
from tema t
join questao q on q.id_tema = t.id_tema
join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
join resposta r on r.id_questao = q.id_questao
join participacao par on par.id_part = r.id_part
join pesquisa p on p.id_pesquisa = par.id_pesquisa
where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
group by t.id_tema, t.nome_tema
having count(r.id_resposta) >= ?
order by t.nome_tema, t.id_tema;

Select
    g.id_grupo,
    g.nome_grupo,
    coalesce(round(avg(r.valor_escala), 2), 0) as media,
    count(r.valor_escala) as total_respostas
from grupo g
left join participacao par on par.id_grupo_respondente = g.id_grupo
left join resposta r on r.id_part = par.id_part
   and r.id_questao in (
       select q.id_questao
       from questao q
       join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
       where tq.nome_tipo = 'ESCALA'
   )
group by g.id_grupo, g.nome_grupo
order by g.nome_grupo, g.id_grupo;

with notas as (
    select p.id_pesquisa, p.titulo, p.data_pesquisa, par.id_part, r.valor_escala
    from pesquisa p
    join participacao par on par.id_pesquisa = p.id_pesquisa
    join resposta r on r.id_part = par.id_part
    join questao q on q.id_questao = r.id_questao
    join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
    where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
)
select
    id_pesquisa,
    titulo,
    data_pesquisa,
    coalesce(round(avg(valor_escala), 2), 0) as media,
    count(distinct id_part) as participacoes,
    coalesce(round((select avg(valor_escala) from notas), 2), 0) as media_geral
from notas
group by id_pesquisa, titulo, data_pesquisa
order by data_pesquisa, id_pesquisa;

Select
    rank() over (order by avg(r.valor_escala) desc) as posicao,
    q.id_questao,
    q.descricao,
    t.id_tema,
    t.nome_tema,
    coalesce(round(avg(r.valor_escala), 2), 0) as media,
    count(r.id_resposta) as total_respostas
from questao q
join tema t on t.id_tema = q.id_tema
join tipo_questao tq on tq.id_tipo_questao = q.id_tipo_questao
join resposta r on r.id_questao = q.id_questao
where tq.nome_tipo = 'ESCALA' and r.valor_escala is not null
  and lower(q.descricao) like lower(?) escape '!'
group by q.id_questao, q.descricao, t.id_tema, t.nome_tema
having count(r.id_resposta) >= ?
order by posicao, q.id_questao
limit ?;

SELECT g.id_grupo, g.nome_grupo, count(gu.id_usu) as esperados,
       count(case when cp.concluida = true then gu.id_usu end) as respondidos,
       count(gu.id_usu) - count(case when cp.concluida = true then gu.id_usu end) as pendentes
FROM grupo_pesquisa gp
JOIN grupo g ON g.id_grupo = gp.id_grupo
LEFT JOIN grupo_usuario gu ON gu.id_grupo = g.id_grupo
LEFT JOIN controle_participacao cp ON cp.id_usu = gu.id_usu AND cp.id_pesquisa = gp.id_pesquisa
WHERE gp.id_pesquisa = ?
GROUP BY g.id_grupo, g.nome_grupo
ORDER BY g.nome_grupo, g.id_grupo;

SELECT DISTINCT u.id_usu, u.nome, u.email
FROM grupo_pesquisa gp
JOIN grupo_usuario gu ON gu.id_grupo = gp.id_grupo
JOIN usuario u ON u.id_usu = gu.id_usu
LEFT JOIN controle_participacao cp ON cp.id_usu = u.id_usu AND cp.id_pesquisa = gp.id_pesquisa
WHERE gp.id_pesquisa = ? AND (cp.concluida = false OR cp.id_usu IS NULL)
ORDER BY u.nome, u.id_usu;
