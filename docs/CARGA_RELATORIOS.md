# Carga para testar os relatórios

Execute `database/schema.sql` e depois `database/carga.sql` em um banco novo e vazio.
Os IDs da carga consideram sequências iniciando em 1. O script não apaga dados existentes.
Não execute novamente sobre um banco já preenchido.

No pgAdmin, crie outro banco, por exemplo `pesquisa_relatorios`, e execute os dois arquivos
no Query Tool desse banco. Depois ajuste somente o nome do banco em `DB_URL` no `.env`
e reinicie o backend. Mantenha as credenciais do PostgreSQL que já funcionam.

Todos os usuários são participantes e usam a senha `Teste123!`.
Exemplos de login: `eduardo.uel@uel.br`, `CaioR.uel@uel.br` e `lucascabeca.uel@uel.br`.

## Volume e cenários

A carga atende à referência do professor: **20 usuários, 5 pesquisas e 30 questões**.

- 5 grupos e 4 temas.
- 18 participações: 6 em setembro, 7 em outubro e 5 em novembro.
- 473 respostas: 329 notas e 144 textos.
- 11 participações identificadas; as 7 de outubro são anônimas.
- 92 controles de participação, dos quais 18 estão concluídos.
- Todas as 30 questões estão ligadas a alguma pesquisa; 27 já possuem respostas.
- Eduardo pertence a Estudantes e Professores, para testar pendentes sem duplicação.
- Convidados tem Bruna, que ainda não respondeu. Sem membros não tem usuários.

| ID | Pesquisa | Data | Tipo | Questões | Participações |
| --- | --- | --- | --- | --- | --- |
| 1 | Avaliação de setembro | 10/09/2026 | Identificada | 26 | 6 |
| 2 | Avaliação de outubro | 10/10/2026 | Anônima | 26 | 7 |
| 3 | Avaliação de novembro | 10/11/2026 | Identificada | 27 | 5 |
| 4 | Pesquisa aberta para testes | Dia da execução da carga | Identificada | 26 | 0 |
| 5 | Pesquisa futura | Sete dias após executar a carga | Anônima | 3 | 0 |

A pesquisa aberta permite responder entre 00:00 e 23:59:59 naquele dia.
Depois desse dia, crie outra pesquisa ou ajuste a pesquisa aberta antes de ela receber respostas.
As questões 9, 29 e 30 pertencem à pesquisa futura e ainda não possuem respostas.

## 1. Média por tema

Sem filtros, mínimo de respostas igual a 1:

| Tema | Média | Notas |
| --- | --- | --- |
| Atendimento | 3,89 | 108 |
| Ensino | 4,56 | 108 |
| Infraestrutura | 3,05 | 113 |

O tema Sem notas não aparece. Com mínimo de 109, aparece apenas Infraestrutura.
Com período de 10/09/2026 até 10/09/2026, as médias são 3,00 para Atendimento, 4,00 para
Ensino e 2,00 para Infraestrutura. Os textos não influenciam as médias.

## 2. Comparação entre grupos

Em todas as pesquisas:

| Grupo | Média | Notas |
| --- | --- | --- |
| Estudantes | 3,47 | 146 |
| Professores | 4,06 | 110 |
| Funcionários | 4,15 | 73 |
| Convidados | Sem respostas | 0 |
| Sem membros | Sem respostas | 0 |

Selecionando setembro, as médias são 2,00, 3,00 e 4,00 para Estudantes, Professores e
Funcionários. Selecionando a pesquisa aberta, seus quatro grupos aparecem sem respostas.

## 3. Evolução no tempo

Sem filtro de tema:

| Pesquisa | Média | Média geral | Participações |
| --- | --- | --- | --- |
| Setembro | 3,00 | 3,82 | 6 |
| Outubro | 3,90 | 3,82 | 7 |
| Novembro | 4,64 | 3,82 | 5 |

As duas pesquisas sem notas não aparecem. Filtrando Infraestrutura, as médias são 2,00,
3,00 e 4,20. Filtrando Sem notas, o resultado é vazio.

## 4. Ranking

Selecione limite 50 para visualizar todas as 19 questões de escala com respostas.
O limite padrão 10 mostra somente as primeiras dez.

| Posição | IDs das questões | Média | Notas por questão |
| --- | --- | --- | --- |
| 1 | 5, 6, 19, 20, 21, 22: questões de Ensino | 4,56 | 18 |
| 7 | 10: acessibilidade do campus | 4,20 | 5 |
| 8 | 3, 4, 15, 16, 17, 18: questões de Atendimento | 3,89 | 18 |
| 14 | 1, 2, 11, 12, 13, 14: questões de Infraestrutura | 3,00 | 18 |

Os empates ocupam a mesma posição. Com mínimo de 6, acessibilidade desaparece.
Com mínimo de 19, o resultado é vazio. Buscando `acessibilidade`, aparece somente a questão 10.
Selecionando apenas Infraestrutura, aparecem sete questões. Escolhendo as piores e limite 2,
aparecem salas de aula e biblioteca. Questões de texto não entram no ranking.

## 5. Quem respondeu

Selecione setembro:

| Grupo | Esperados | Responderam | Pendentes |
| --- | --- | --- | --- |
| Estudantes | 11 | 2 | 9 |
| Professores | 6 | 3 | 3 |
| Funcionários | 3 | 2 | 1 |
| Convidados | 1 | 0 | 1 |
| Sem membros | 0 | 0 | 0 |

A lista mostra 14 pendentes únicos. Eduardo é contado em dois grupos, mas enviou uma
única participação. Em outubro há 13 pendentes únicos; em novembro, 15; na pesquisa aberta,
20; na futura, 12. As contagens por grupo não devem ser somadas como total de pessoas únicas.

## Ver respostas e testar novos envios

Em Pesquisas > Ver respostas, setembro identifica Eduardo, Anderson, Carmen, Pedro Goes,
Murilo Fla e Pedro Paulo. Novembro identifica Eduardo, Caio, Joao Carlos, Evandro e Carmen.
Outubro mantém os sete respondentes sem identificação.

Entre como Lucas e responda à Pesquisa aberta para testes. Ela passará a aparecer na evolução,
terá notas nos demais relatórios e ficará com 19 pendentes únicos. O nome de Lucas aparecerá
junto das respostas enviadas. Tentar responder novamente pelo fluxo normal será bloqueado.

Os valores deste roteiro são os resultados iniciais, antes de novos envios ou alterações.
A carga foi executada em schema isolado e passou em **144 verificações HTTP**, cobrindo os
cinco relatórios, filtros, empates, contagens, integridade das respostas, anonimato, identificação
e um novo envio pelo fluxo real. O banco atual da aplicação foi preservado.
