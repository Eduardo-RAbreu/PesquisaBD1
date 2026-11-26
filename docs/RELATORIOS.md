# Relatórios

Inicie o backend e o frontend conforme o README, faça login e abra **Relatórios**.
Depois de alterar controllers, pare e inicie novamente o backend para carregar as novas rotas.
Não é necessário executar nova migração do banco para os relatórios.

## Organização

`RelatorioController` recebe os parâmetros HTTP e devolve os models como JSON.
`RelatorioService` valida datas, IDs, mínimo de respostas, ordem e limite.
`RelatorioDao` executa as consultas com `PreparedStatement` e lê o `ResultSet`.
As consultas básicas também estão em `database/Relatorios.sql`, sem os filtros opcionais,
para conferência; não é necessário
executar esse arquivo para habilitar os endpoints.

## Rotas GET

Todas exigem login. Os caminhos abaixo começam com `/api/relatorios`.

| Caminho | Parâmetros |
| --- | --- |
| `/media-por-tema` | `de` e `ate` opcionais, formato YYYY-MM-DD; `minRespostas`, padrão 1 |
| `/media-por-grupo` | `idPesquisa` opcional, omitido considera todas |
| `/evolucao` | `idTema` opcional, omitido considera todos |
| `/ranking-questoes` | `ordem`: melhores ou piores; `busca`; `idsTemas`: IDs separados por vírgula; `minRespostas`, padrão 1; `limite`, padrão 10, entre 1 e 50 |
| `/adesao` | `idPesquisa` obrigatório |

Filtros inválidos retornam 400. Pesquisa inexistente nos relatórios por grupo e adesão retorna
404. Sem sessão, a API retorna 401.

## Cálculos

- Somente questões de ESCALA com nota entram nas médias; respostas de texto são excluídas.
- Média por tema filtra pela data da pesquisa, incluindo as duas datas, e conta notas.
- Comparação usa `participacao.id_grupo_respondente`, sem atribuir a mesma resposta a todos os
  grupos da pesquisa. Sem filtro, aparecem todos os grupos; com filtro, somente os destinatários.
  Grupos sem notas retornam média 0 por COALESCE e contagem 0; a interface mostra “sem respostas”.
- Evolução inclui pesquisas com notas no filtro, ordenadas por data e ID. A média geral considera
  todas as notas do filtro, sem média de médias. Participações conta IDs de participação distintos.
- Ranking ordena pela média sem arredondamento; a exibição tem duas casas. Empates usam RANK
  (1, 1, 3), com ordem estável por ID. O limite restringe linhas, mesmo em empates. A busca é
  literal e ignora maiúsculas/minúsculas; `%` e `_` não são curingas.
- Adesão considera os membros atuais dos grupos e `controle_participacao.concluida`. Uma pessoa
  pode ser contada em vários grupos, mas aparece só uma vez na lista de pendentes. Contagem e
  lista são consultadas em uma mesma transação consistente, sem associar usuários às notas anônimas.
- A carga atual inclui os vínculos das participações identificadas e os controles concluídos
  correspondentes. Bancos com a carga antiga podem continuar com participações fictícias sem esses
  vínculos; elas não são identificadas retroativamente.

Os valores esperados da nova carga estão em [CARGA_RELATORIOS.md](CARGA_RELATORIOS.md).

## Testar na interface

1. Média por tema: use o período e aumente o mínimo de respostas; clique em **Filtrar**.
2. Comparação: alterne entre todas as pesquisas e uma pesquisa específica.
3. Evolução: selecione um tema e confira as datas e a referência constante de média geral.
4. Ranking: alterne melhores/piores, busque uma palavra, selecione temas, mínimo e limite;
   clique em **Filtrar**.
5. Quem respondeu: selecione a pesquisa, confira as contagens e a lista de pendentes. Responda
   pelo fluxo normal e volte à aba para conferir a mudança.

## Verificação em 26/11/2026

Passaram 86 verificações HTTP em schema isolado: médias, contagens, período inclusivo,
mínimo, grupos sem notas, média ponderada, participações distintas, busca literal, temas,
ordenação, empates, limite, pendentes sem duplicidade, acesso e filtros inválidos.
A compilação Java e o build do frontend passaram, e as cinco abas foram conferidas no navegador.
Os dados da aplicação foram preservados.

## SQL no estilo dos exercícios

As consultas usam SELECT, JOIN, LEFT JOIN, WHERE, GROUP BY, HAVING, AVG, COUNT e COALESCE.
O ranking usa RANK diretamente sobre AVG, como no exemplo de ranking de gastos.
A evolução usa um único WITH para reaproveitar as notas no cálculo da média geral, como
no exercício que calcula gastos e compara com a média. As contagens de adesão usam CASE.
Não há arrays SQL, ANY, FILTER nem blocos de parâmetros com CAST.

No Java, um filtro preenchido acrescenta um AND à consulta e recebe seu valor por setInt,
setString ou setObject. Nenhum valor digitado pelo usuário é concatenado no SQL.
Somente asc/desc, escolhidos a partir do booleano melhores, entram diretamente no texto.
