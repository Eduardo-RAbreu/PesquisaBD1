# PesquisaBD1
Trabalho da disciplina de Banco de Dados, fazendo sobre o tema 4 (Pesquisa)

Participantes:

Eduardo Rodrigues Abreu

Caio Eduardo Rossate 

Natureza: Pesquisa de satisfação ou de opinião

## Executar localmente

Requisitos: PostgreSQL com `pgcrypto`, JDK 17 ou superior, Node.js 22.12 ou superior e npm.

### Banco

Crie o banco indicado em `DB_URL`. Em um banco vazio, execute pelo pgAdmin, nesta ordem:

1. `database/schema.sql`
2. `database/carga.sql`

Se o banco já foi criado com uma versão anterior do schema, execute apenas
`database/migracoes/001_participacao_identificada.sql` e depois
`database/migracoes/004_remover_perfil.sql`. Elas acrescentam a identificação de participações
não anônimas e retiram o antigo campo de perfil. As migrações 002 e 003 são históricas e
não são necessárias para chegar ao modelo atual.

Os usuários da carga têm a senha de teste `Teste123!`. A carga inclui pesquisas com notas
variadas para os cinco relatórios e uma pesquisa aberta no dia da execução. São 20 usuários,
5 pesquisas, 30 questões e 473 respostas, atendendo à referência de volume do trabalho.
Veja [os resultados esperados da carga](docs/CARGA_RELATORIOS.md) para conferir os testes.

### Backend

Configure `backend/.env` usando `backend/.env.example`, com `DB_URL`, `DB_USER` e `DB_PASSWORD`.
Não versione o `.env`.

Abra a pasta `PesquisaBD1` no VS Code, selecione **Executar e Depurar > Iniciar backend** e pressione F5.
A configuração `.vscode/launch.json` carrega o `.env`. A API usa a porta 8080 por padrão.

Se preferir Maven, disponibilize as três variáveis no ambiente do terminal e execute
`./mvnw.cmd spring-boot:run` dentro de `backend`. O Maven e o Spring não carregam o `.env` automaticamente.

### Frontend

No terminal, dentro de `frontend`:

```powershell
npm ci
npm run dev
```

Abra o endereço informado pelo Vite, normalmente `http://localhost:5173`.
O proxy `/api` encaminha as chamadas para `http://localhost:8080`, incluindo o cookie de sessão.
É possível definir `BACKEND_URL` no ambiente do Vite para usar outra porta do backend.

## Fluxos integrados

Todas as funcionalidades exigem login. Todos os usuários possuem o
mesmo acesso aos menus, cadastros e montagem de pesquisas. Não existe distinção de administrador.
O backend exige uma sessão válida em todas as chamadas da API, exceto no login; sem sessão,
retorna 401. Para responder, continuam valendo os grupos autorizados e a janela de horário.

O cadastro de usuário contém nome, e-mail e senha, sem perfil ou permissões por função.
Em bancos existentes, `004_remover_perfil.sql` remove a coluna antiga sem alterar senhas.

- Cadastro e consulta de temas, grupos e questões, com contagens e nomes das classificações.
- Cadastro de usuário com senha e grupos em uma única transação.
- Login por e-mail e senha, recuperação da sessão ao recarregar e logout.
- Cadastro e edição de pesquisa com grupos, questões e ordenação em uma única transação.
- Listagem das pesquisas destinadas aos grupos do usuário, com situação e conclusão.
- Formulário ordenado, com questões de texto e escala e seleção do grupo respondente quando necessária.
- Envio completo com autorização, horário, validação, bloqueio de reenvio e transação.
- Respostas anônimas sem vínculo individual; respostas identificadas com vínculo em `participacao_identificada`.
- Em **Pesquisas > Ver respostas**, consulta das notas e textos por questão, com filtro e atualização.
- Pesquisas não anônimas mostram o nome de quem respondeu e permitem filtrar por respondente;
  participações antigas sem identificação salva são indicadas como identificação não registrada.

Pesquisas que já receberam participações não podem ser editadas pelo cadastro composto.
Todas as questões do formulário são obrigatórias. A janela de participação usa o fuso
`America/Sao_Paulo`, incluindo o início e excluindo o instante final.

Os cinco relatórios estão integrados com DAO, service e controller: média por tema, comparação
por grupo, evolução, ranking e adesão. Consulte [o roteiro dos relatórios](docs/RELATORIOS.md)
para entender os filtros, os cálculos e como testar.

## Conferir as funcionalidades

Consulte [o roteiro de testes](docs/TESTES_INTEGRACAO.md) para repetir a validação pela interface,
verificar os resultados esperados e consultar a matriz do que já foi testado.
