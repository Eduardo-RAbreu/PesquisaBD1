# Roteiro de integração: cadastros, montagem e respostas

Verificação realizada em 26/11/2026. Escopo original: passos 1, 2 e 3 da integração. Para a validação posterior dos relatórios,
consulte [RELATORIOS.md](RELATORIOS.md).

## Resultado confirmado

O backend foi iniciado como aplicação Spring Boot e acessado por HTTP através do proxy do Vite.
Após simplificar o acesso, passaram 95 verificações dos fluxos principais, 24 de exclusão e
integridade e 15 de login obrigatório e acesso igual aos cadastros: 134 verificações HTTP.
A compilação Java e o build do frontend passaram. No navegador, foi confirmado que o login
abre o início com todos os menus, o participante acessa Usuários sem seletor de perfil e o
logout retorna para Entrar e oculta os menus.

Os testes automatizados usaram um schema temporário separado dos dados da aplicação. Também houve
conferência no navegador de login, listagens, cadastro de grupo, cadastro de usuário com grupo,
cadastro de questão, alteração da ordem selecionada no formulário e envio de resposta com retorno
à listagem marcada como Respondida. A gravação da montagem de pesquisa foi validada por HTTP;
não foi concluída uma criação de pesquisa pelo navegador automatizado.

| Funcionalidade | Resultado esperado e verificado |
| --- | --- |
| Tipos e temas | IDs válidos para selecionar no formulário; nomes e contagens visíveis |
| Questões | Cadastro, consulta, edição e exclusão de questão sem vínculos; tema e tipo visíveis |
| Grupos | Cadastro, listagem, contagem de membros e exclusão quando permitida |
| Usuários | Senha com hash; grupos gravados junto do usuário; duplicidade de e-mail rejeitada |
| Cadastro inválido | Grupo inexistente desfaz também a criação do usuário |
| Login | Senha incorreta é rejeitada; senha correta cria sessão sem retornar a senha |
| Acesso | Sem sessão retorna 401; qualquer participante logado pode acessar os cadastros |
| Cadastro de usuário | Todos são participantes; não há seleção de administrador |
| Revalidação | Usuário atual é consultado no banco; usuário excluído perde a sessão |
| Sessão e logout | `/api/me` recupera o usuário; logout invalida o acesso às pesquisas do participante |
| Montagem | Questões, ordem, grupos e controles são salvos junto da pesquisa |
| Edição da montagem | Listas e título são atualizados; erro de referência desfaz toda a edição |
| Autorização | Listagem contém somente pesquisas dos grupos do participante |
| Janela de horário | Pesquisa futura ou encerrada não pode ser aberta para responder |
| Formulário | Ordem, descrições, tema e tipo correspondem à montagem |
| Validação | Nota fora de 1 a 5, questão ausente e formulário incompleto são rejeitados |
| Envio | Participação, respostas e conclusão são persistidas juntas |
| Reenvio | Nova tentativa é rejeitada; duas chamadas simultâneas aceitam somente uma |
| Vários grupos | Exige escolha entre os grupos autorizados; grupo indevido é rejeitado |
| Anonimato | Não existe linha de identificação para participação anônima |
| Pesquisa identificada | A participação tem o usuário correto em `participacao_identificada` |
| Exclusões | Vínculos simples são removidos por cascata; referências protegidas impedem exclusão |

## Antes de testar

1. Prepare o banco e o `.env` conforme o README.
2. Execute as migrações necessárias indicadas no README, incluindo `003_todos_participantes.sql`.
3. Inicie o backend em 8080 e o frontend pelo Vite. Use o endereço do frontend para navegar.
4. Crie registros novos para os testes. As pesquisas da carga já possuem participações fictícias e
   algumas datas estão fora da janela atual; sua edição pode ser bloqueada corretamente.
5. Entre com um usuário da carga: `eduardo.uel@uel.br`, senha da carga `Teste123!` se não foi alterada.
   Sem login, abrir qualquer página de funcionalidade deve levar para Entrar.

## 1. Preparar os cadastros

1. Em **Temas**, crie um tema. Ele deve aparecer com zero questões.
2. Em **Questões**, crie uma questão de ESCALA e outra de TEXTO usando esse tema.
3. Volte à listagem: as descrições, o tema e os tipos devem aparecer.
4. Edite uma questão ainda sem respostas e confira a mudança.
5. Em **Grupos**, crie dois grupos para testar tanto um grupo único quanto múltiplos grupos.
6. Em **Usuários**, cadastre nome, e-mail, senha e selecione os grupos. A linha do usuário deve
   mostrar os nomes dos grupos; as contagens de membros devem aumentar.
7. Tente cadastrar novamente o mesmo e-mail: deve aparecer uma mensagem de duplicidade.

## 2. Montar e editar uma pesquisa

1. Em **Pesquisas > Nova pesquisa**, informe título e a data de hoje.
2. Defina início anterior ao horário atual e fim posterior. A janela não atravessa a meia-noite.
3. Selecione os grupos e as duas questões cadastradas.
4. Use **Subir** e **Descer** na lista de ordem das questões para definir a sequência desejada.
5. Salve. A listagem deve mostrar a quantidade correta de questões e grupos.
6. Abra **Editar** antes de responder: título, campos e seleções devem ser recuperados.
7. Altere título ou ordem, salve e reabra para conferir persistência.
8. Crie também uma pesquisa destinada a um grupo do qual o usuário de teste não participa.

## 3. Entrar e responder

1. Saia da conta atual e abra **Entrar**. Teste uma senha incorreta e confira a mensagem.
2. Entre com o e-mail e a senha do usuário cadastrado.
3. Em **Responder**, confira que a pesquisa destinada aos seus grupos está disponível e a pesquisa
   do outro grupo não aparece. Atualize a página para verificar a manutenção da sessão.
4. Abra a pesquisa: as questões devem estar na ordem salva.
5. Se houver mais de um grupo elegível, selecione qual você representa.
6. Tente enviar sem responder todas as questões: a tela deve informar o que falta.
7. Escolha uma nota de 1 a 5 e escreva uma resposta de texto. Envie.
8. A tela deve voltar à listagem com uma mensagem de sucesso e a pesquisa deve ficar Respondida.
9. Uma nova tentativa pela API também deve ser rejeitada; esconder o botão não é a única proteção.
10. Clique em **Sair**. Ao voltar a **Responder**, deve ser solicitado novo login.
11. Entre novamente como participante e tente abrir `/usuarios`: deve abrir a listagem de usuários.
    Após sair, essas mesmas chamadas devem retornar 401.

## 4. Confirmar o modo identificado

1. Entre com um usuário, crie outra pesquisa aberta e desmarque **Respostas anônimas**.
2. Saia e entre como participante autorizado para responder.
3. No pgAdmin, adapte os IDs nas consultas abaixo:

```sql
SELECT p.id_part, p.id_pesquisa, pi.id_usu
FROM participacao p
LEFT JOIN participacao_identificada pi ON pi.id_part = p.id_part
WHERE p.id_pesquisa IN (ID_PESQUISA_ANONIMA, ID_PESQUISA_IDENTIFICADA);

SELECT id_usu, id_pesquisa, concluida
FROM controle_participacao
WHERE id_usu = ID_USUARIO;
```

A participação anônima não deve possuir `id_usu` nesse vínculo. A identificada deve ter o usuário
correto. Os controles das duas pesquisas devem estar concluídos. A migração não tenta identificar
participações fictícias antigas, pois essa informação não existia.

## Limites desta etapa

A visualização em **Pesquisas > Ver respostas** foi conferida no navegador com notas, textos,
filtro por questão, atualização, separação entre pesquisas, ausência de respostas e acesso
protegido por login. Em pesquisas não anônimas, também foram conferidos o nome junto de cada
resposta e o filtro por respondente. A rota `GET /api/pesquisas/{id}/participacoes` consulta o
vínculo salvo em `participacao_identificada`, retornando identidade somente quando a pesquisa
não é anônima. Participações antigas sem vínculo não recebem identidade presumida. Nos testes,
foi confirmado que a rota oculta nome e ID de usuário para pesquisas anônimas mesmo se houver
um vínculo indevido nos dados. Nenhuma senha ou e-mail é retornado nessa consulta.

A tela ainda filtra a listagem completa de respostas no frontend. Para grandes volumes,
uma consulta específica de respostas por pesquisa no backend pode substituir essa listagem.
Os relatórios estatísticos estão integrados separadamente dessa consulta; veja o roteiro específico.

- Relatórios: os cinco endpoints estão integrados às telas, com validação descrita no roteiro específico.
- Todos os usuários são participantes, com o mesmo acesso após login.
- Os CRUDs técnicos de respostas e controles ainda existem e exigem login;
  o fluxo normal da tela usa o Service transacional.
- Grupos e sua composição atual definem os destinatários. Não há histórico de composição dos grupos.
- A separação de identidade das respostas anônimas é verificada; políticas de logs e grupos pequenos
  e a atualização dos diagramas ainda precisam acompanhar a finalização do projeto.

## Auditoria contra o enunciado e o guia de regras de negócio

Conferência em 26/11/2026, após a remoção de comentários de 24 arquivos de código/configuração.
Foram preservados textos da interface, documentação de uso e avisos de licença dos wrappers Maven.
As fontes foram os PDFs do trabalho (5 páginas) e das regras de negócio (4 páginas), na pasta bd1.
O guia distingue exigências do professor, decisões do modelo e regras desejadas; essas categorias
não têm o mesmo peso. A expressão PENDENTE no guia descreve a versão antiga, não prova por si só
que a implementação atual continua faltando.

### Resultado

Os fluxos principais estão implementados, mas o projeto ainda não atende integralmente a todos
os itens do guia e da entrega. Compilação Java e build do frontend passaram. A limpeza dos
comentários não mudou os arquivos gerados do frontend (mesmos hashes de CSS/JavaScript).
O lint terminou sem erro, com o aviso já existente de Fast Refresh em AuthContext.

Em schema isolado, passaram 76 verificações da carga/relatórios, 95 de integração e 24 de
exclusão/integridade: 195 verificações dos fluxos normais. Separadamente, sete casos de violação
das regras foram reproduzidos nas rotas CRUD. Não são sete testes de conformidade aprovados:
são falhas confirmadas. Nenhum dado do banco usado pelo usuário foi alterado na auditoria.

### Requisitos funcionais e técnicos

| Item do trabalho | Situação | Evidência ou pendência |
| --- | --- | --- |
| RF1: dois tipos de questões | Implementado | ESCALA e TEXTO, cadastrados pela interface |
| RF2: cadastro de pesquisas | Implementado | Título, descrição, data, início, fim e anonimato |
| RF3: questões existentes e reutilizáveis | Implementado no fluxo normal | Formulário composto, ordem e relação N:N; edição direta da relação ainda contorna proteção histórica |
| RF4: destinatários | Implementado | Grupos escolhidos na pesquisa e usuários associados a grupos |
| RF5: responder e persistir | Implementado no fluxo normal | Validação e transação no ParticipacaoService; rotas CRUD permitem gravações inconsistentes |
| RF6: satisfação/opinião | Implementado com limites | Temas, escala, grupos, anonimato básico e identidade nas não anônimas; anonimato indireto ainda parcial |
| RF7: três relatórios com tabela e gráfico | Implementado | Cinco relatórios em React/Recharts e endpoints correspondentes |
| PostgreSQL/JDBC/sem JPA | Implementado | Schema PostgreSQL, PreparedStatement e DAOs; pom sem JPA/Hibernate |
| Camadas/transações | Implementado nos fluxos compostos | Controllers, models, services e DAOs; SQL nos DAOs; commit/rollback nos cadastros compostos e envios |
| Schema e carga em banco vazio | Verificado | Execução em schema novo isolado, com as mesmas tabelas e dados |
| Referência de volume da carga | Abaixo da referência | Atual: 9 usuários, 5 pesquisas e 10 questões; referência do enunciado: pelo menos 20, 5 e 30 |
| Recursos SQL dos relatórios | Quase completo | JOIN, LEFT JOIN, COUNT, AVG, GROUP BY, HAVING, subconsulta, RANK, ORDER BY, datas, IN e LIKE; SUM não é usado, faltando se a lista de funções for cobrada literalmente |
| Documentação dos relatórios no README | Parcial | Há links e roteiro, mas faltam imagem de cada tela, SQL e indicação dos recursos no README |
| DER/esquema/3FN | Desatualizado | Relacional não inclui participacao_identificada, id_grupo_respondente, senha_hash/perfil; DER também não modela o novo vínculo de identidade; frase de 3FN afirma a condição sem explicar as dependências |
| Nome do script de relatórios | Divergência de nome | Repositório usa Relatorios.sql; enunciado pede relatorios.sql, relevante em sistemas que diferenciam maiúsculas |
| Identificação das consultas no SQL | Ausente após pedido do usuário | Enunciado pede um comentário identificador por relatório; comentários foram removidos conforme solicitado |
| Git/entrega/pitch | Parcial/não verificável localmente | Histórico de 13 a 26/11, dois integrantes com commits; várias alterações atuais ainda sem commit; acesso do professor, envio no Classroom e pitch não foram verificados |

### Regras RN01 a RN45

| Regra | Situação atual |
| --- | --- |
| RN01–RN03 | Fluxo principal implementado; natureza de satisfação com temas, ESCALA e TEXTO |
| RN04–RN06 | PK, nomes não vazios, e-mail/nome de grupo únicos e relação usuário-grupo sem duplicidade |
| RN07 | Login simples, sessão e logout implementados; não é necessário criar administradores para satisfazer o trabalho |
| RN08–RN12 | Campos, horários, anonimato padrão verdadeiro, destinatários e controles presentes |
| RN13 | Horário e autorização conferidos pelo fluxo de envio; gravações diretas via CRUD não passam por essas validações |
| RN14–RN19 | Tema/tipo obrigatórios, nomes e descrições, reutilização, chave composta e ordem positiva/única implementados |
| RN20–RN23 | Participação, referências, datas e resposta com exatamente um valor, escala entre 1 e 5, garantidos por validações e schema |
| RN24–RN25 | Corretos no ParticipacaoService, mas contornáveis por POST/PUT em respostas |
| RN26 | Incompleto: permite mudar tipo de questão respondida, retirar sua associação e trocar a pesquisa da participação |
| RN27 | Controle individual separado de participação/resposta |
| RN28 | Reenvio e concorrência protegidos no envio normal; PUT/DELETE do controle permitem reabrir a conclusão |
| RN29 | Sem vínculo direto no envio anônimo; identidade de pesquisas identificadas fica em tabela separada |
| RN30 | Parcial: horários individuais não são gravados pelo envio, mas não há mecanismo para impedir correlação por IDs, observação da ordem de gravação, logs e grupos pequenos |
| RN31 | Pendente: não há política de tamanho mínimo; a tela de respostas mostra votos individuais anônimos com o grupo respondente |
| RN32 | Autorização e separação básica existem; não há mecanismo formal de autorização não rastreável, item desejado no guia |
| RN33 | Implementado: participacao_identificada, gravação no service e consulta de nomes somente para pesquisas não anônimas |
| RN34–RN35 | RESTRICT protege pesquisa com participação; IDs gerados e referências seguem o schema |
| RN36/RN38 | Cinco relatórios com temas, grupos e períodos, tabelas e gráficos |
| RN37 | Recursos principais cobertos; ressalva sobre ausência de SUM na lista literal |
| RN39 | Faltam imagens e apresentação completa do SQL/recursos de cada relatório no README |
| RN40–RN42 | PostgreSQL/JDBC/camadas e transações nos fluxos compostos implementados; rotas diretas precisam respeitar as mesmas regras |
| RN43 | Carga abaixo da referência de 20 usuários e 30 questões |
| RN44 | Arquivos principais presentes, mas diagramas precisam atualização e comentários identificadores do SQL foram removidos a pedido |
| RN45 | Entrega no Classroom, acesso ao GitHub e apresentação dependem de ação externa; não comprovados nesta auditoria |

### Falhas reproduzidas

Todos os testes abaixo foram feitos somente no servidor de teste e no schema descartável.

| Caso | Retorno observado | Regra afetada |
| --- | --- | --- |
| PUT /api/questoes/1 muda ESCALA para TEXTO apesar das respostas existentes | 204, alteração aceita | RN26; também acessível pela tela de edição da questão |
| POST /api/respostas grava questão 9 em participação da pesquisa 1, onde a questão não foi incluída | 201, gravação aceita | RN24 |
| PUT /api/respostas/{id} troca texto da questão 7 por nota numérica | 204, alteração aceita | RN25 |
| DELETE /api/pesquisas-questoes/1/1 retira questão já respondida | 204, remoção aceita | RN26 |
| PUT /api/participacoes/1 muda a pesquisa apesar das respostas existentes | 204, alteração aceita | RN26 |
| PUT /api/controles-participacao/1/4 troca concluida para false | 204, reabertura aceita | RN28 |
| POST do envio normal após essa reabertura grava nova participação do mesmo usuário/pesquisa | 201, segundo envio aceito | RN28 |

O problema não exige criar perfis administrativos. Para manter todos com o mesmo acesso ao site,
as operações normais podem continuar liberadas após login. As rotas de escrita técnicas que não
são necessárias à interface devem ser removidas/restringidas ou passar pelas mesmas validações
e transações dos services. A edição de tipo de questão também precisa de proteção histórica.

### Ordem sugerida para concluir

1. Fechar as brechas de respostas/participações/controles e proteger o tipo de questão respondida.
2. Ampliar a carga para a referência de 20 usuários e 30 questões, mantendo a coerência dos controles.
3. Atualizar DER e relacional, incluindo identidade opcional, grupo respondente e senha com hash;
   justificar a 3FN com ausência de dependências parciais e transitivas.
4. Completar o README com as cinco imagens, objetivos, SQL e recursos; conferir SUM se exigido
   literalmente e o nome do arquivo relatorios.sql.
5. Decidir como conciliar o pedido de remover todos os comentários com a identificação das consultas
   exigida pelo professor; os comentários não foram recolocados nesta auditoria.
6. Documentar os limites de anonimato e definir a política para grupos pequenos se o guia inteiro
   precisar ser cumprido, além do mínimo explicitamente exigido pelo enunciado.
7. Revisar o diff, fazer commit/push das alterações finais, conferir acesso do professor e ensaiar o pitch.

As pendências acima foram diagnosticadas; não foram corrigidas automaticamente nesta auditoria.


### Atualização posterior: carga ampliada

A pendência de volume da carga foi resolvida após a auditoria: agora são 20 usuários,
5 pesquisas, 30 questões, 18 participações e 473 respostas. Todas as questões são usadas
em pesquisas; 27 já possuem respostas e três pertencem à pesquisa futura sem respostas.
A carga ampliada passou em 144 verificações HTTP dos relatórios, integridade e envio.
Os resultados iniciais estão em [CARGA_RELATORIOS.md](CARGA_RELATORIOS.md).
As contagens de 9 usuários/10 questões nas seções de auditoria acima representam a versão
anterior. As demais pendências da auditoria não foram alteradas por esta atualização de dados.

### Atualização posterior: sem perfis de acesso

O campo perfil foi retirado dos models, DAOs, validações, schema e interface. Todos os usuários
logados têm o mesmo acesso; o cadastro e o login continuam identificando quem responde.
Não é necessário representar perfil no DER ou no relacional. O usuário possui id_usu, nome,
email e senha_hash no banco. Referências a perfil na auditoria acima descrevem a versão anterior.

A migração 004_remover_perfil.sql foi testada em schema isolado, preservando IDs, nomes, e-mails
e hashes das senhas. Ela está disponível para bancos existentes; não foi aplicada ao banco atual
do usuário nesta alteração. A compilação Java, o build do frontend e 239 verificações HTTP
(144 da carga/relatórios e 95 da integração) passaram sem o campo perfil.
