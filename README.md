# EcoCity ESG

Aplicação única Java 21 / Spring Boot 3.5 com MongoDB. Domínio, casos de uso e portas não dependem de Spring; REST, agendamento, logs e MongoDB são adaptadores. Testes ArchUnit protegem as fronteiras.

## API e segurança

As coleções `/api/v1/energy-consumptions`, `/waste-collections`, `/carbon-emissions`, `/diversity-reports` e `/environmental-licenses` oferecem POST, GET, GET por ID, PUT e DELETE. Listas usam `page` (começa em 0) e `size` (1–100, padrão 20), ordenadas por ID. Paginação por offset não produz um retrato estável durante escritas simultâneas.

HTTP Basic usa duas contas locais definidas **exclusivamente por configuração externa**. A senha de cada conta precisa ter pelo menos 12 caracteres, ser diferente da outra e não pode ser o exemplo `replace-with-*`. Falta de configuração ou valores inválidos impedem a inicialização. As senhas ficam codificadas com BCrypt em memória; não são armazenadas no código nem em MongoDB. Em ambiente compartilhado, forneça HTTPS no proxy de entrada e gerencie/rotacione as credenciais fora do repositório. O mecanismo local não oferece revogação individual de sessões, auditoria de usuários nem integração com diretório corporativo.

| Ação | Acesso |
| --- | --- |
| GET dos recursos, OpenAPI/Swagger e `/actuator/health/**` | Público |
| POST e PUT dos recursos | `EDITOR` ou `ADMIN` |
| DELETE dos recursos e `/actuator/info` | `ADMIN` |
| Outros caminhos/Actuator | Não expostos ou restritos a `ADMIN` |

Credenciais ausentes ou incorretas retornam 401; privilégios insuficientes retornam 403. A API não devolve detalhes de autenticação. Apenas `health` e `info` estão expostos pelo Actuator; detalhes internos de health ficam ocultos. `X-Request-ID` acompanha cada resposta e o contexto de log. Falhas inesperadas registram tipo de exceção e método, sem mensagens de driver que possam conter credenciais; alertas de licença registram ID e data, não nome da instalação nem número da licença.

```bash
cp .env.example .env
# Defina as senhas locais diferentes das amostras em .env.
docker compose up --build -d
```

O Compose expõe a API em `localhost:8080` e MongoDB só em `127.0.0.1:27017`. A conta Mongo da aplicação tem `readWrite` apenas no banco `ecocity_esg`. A criação do usuário ocorre apenas em volume vazio. Para rodar a JVM no host, configure `SPRING_DATA_MONGODB_URI`, `APP_EDITOR_USERNAME`, `APP_EDITOR_PASSWORD`, `APP_ADMIN_USERNAME` e `APP_ADMIN_PASSWORD` no ambiente e execute `./mvnw spring-boot:run`. Nunca inclua senhas reais em comandos versionados. O perfil `dev` usa Mongo local por padrão; em produção, forneça uma URI com credenciais externas. `DataSeeder` só executa com `SPRING_PROFILES_ACTIVE=dev` **e** `APP_SEED_DEMO_DATA=true`.

Exemplo de criação autenticada:

```bash
curl -u 'editor:SENHA_LOCAL' -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: exemplo-1' -d '{"district":"Centro","wasteType":"RECYCLABLE","weightKg":10,"recyclingRatePercentage":50,"collectionDate":"2026-01-01T00:00:00Z","collectorTeam":"Equipe","properlyDisposed":true}' \
  http://localhost:8080/api/v1/waste-collections
```

## Concorrência e repetição de requisições

Cada documento mutável tem versão Mongo `@Version`. POST e GET por ID retornam ETag forte; os quatro recursos sem estado dependente do tempo usam `"0"` para um registro novo. Licenças usam `"0-ACTIVE"`, `"0-EXPIRED"` etc.: o estado efetivo faz parte da representação e muda a ETag mesmo sem gravação. PUT e DELETE exigem `If-Match` com a ETag forte atual do recurso. A versão incluída no token é conferida de novo pelo serviço e pelo Mongo no momento da gravação. Sem cabeçalho: 428. Sintaxe inválida (incluindo ETag fraca): 400. Token antigo: 412. Uma corrida depois da leitura pode retornar 409 pelo bloqueio otimista do MongoDB. DELETE usa a versão no comando de persistência, portanto uma atualização simultânea não é apagada silenciosamente. Listas não fornecem ETag. Documentos legados sem versão recebem `version: 0` no início; esse preenchimento é idempotente e antecede o uso normal da API.

POST aceita `Idempotency-Key` opcional em todas as cinco coleções. A chave tem 1–128 caracteres ASCII alfanuméricos, `.`, `_` ou `-`; seu escopo é **global por coleção**, inclusive entre usuários. O adaptador Mongo grava uma reserva com hash da chave e SHA-256 do DTO JSON canônico antes da criação: chaves de objetos são ordenadas recursivamente, a ordem de arrays e os tipos de valor são preservados. O ID derivado da chave torna tentativas simultâneas da mesma criação uma única entidade. A repetição com o mesmo conteúdo lógico devolve o mesmo ID (e a representação atual se o recurso já foi alterado); a mesma chave com outro conteúdo retorna 409. A reserva permanece até remoção administrativa deliberada, sem TTL automático. Após DELETE, a reserva vira tombstone e a chave não pode criar novamente. Sem chave, POST continua não idempotente; use chave ao automatizar retries. Um erro após tombstone e antes do DELETE pode deixar a chave bloqueada enquanto o recurso ainda existe; essa escolha impede duplicação. Não há efeito externo de POST além da gravação Mongo.

O aplicativo não repete automaticamente escritas Mongo, conflitos otimistas ou alertas: retry cego poderia duplicar efeitos ou esconder falhas de negócio. O cliente pode repetir POST **com a mesma chave** e atualizar um PUT após novo GET/ETag. O driver Mongo pode usar seu comportamento próprio de retryable writes quando suportado pelo servidor. O adaptador de notificação atual apenas escreve um log; não envia e-mail ou mensagem externa.

## Licenças e agendamento

A consulta de renovação considera vencimento no intervalo inclusivo `[agora, agora + 30 dias]`. Licenças vencidas não são notificadas para sempre; licenças `SUSPENDED` e `RENEWAL_IN_PROGRESS` também são excluídas. O estado retornado pela API é calculado com `Clock` injetado: suspensão prevalece; após o vencimento, `EXPIRED`; antes, a renovação em andamento é preservada. Não há endpoint de transição para suspensão/renovação. Uma licença ativa na janela ainda gera um log a cada execução agendada; não há cooldown persistido, porque hoje o efeito é somente log.

`APP_LICENSE_ALERT_CRON=-` desliga o agendamento no perfil base. Com cron ativo, uma reserva Mongo `scheduler_lock` permite uma única execução por vez entre instâncias enquanto o lease é válido. O lease padrão é `PT1M` (`APP_LICENSE_ALERT_LEASE`, válido de 15 segundos a 1 hora), renovado periodicamente durante o trabalho. Aquisição e renovação usam operações atômicas e `$$NOW`/`$dateAdd` no servidor Mongo; o relógio de cada instância não decide a validade do lease. Isso requer MongoDB 5.0 ou superior (Compose e testes usam MongoDB 7.0). Uma instância interrompida libera apenas sua própria reserva ao encerrar; se morrer, outra pode reclamá-la após expiração. Se a renovação do lease falhar, o scan é interrompido e registra erro. MongoDB precisa estar disponível. Uma pausa de processo maior que o lease ainda pode permitir sobreposição breve; um adaptador externo de notificação exigiria sua própria deduplicação transacional.

## Domínio, números e dados

Regras de domínio validam textos obrigatórios, enums, datas, números finitos e faixas mesmo fora do HTTP. `ReportingQuarter` é um valor imutável com ano e trimestre válidos; JSON e Mongo continuam usando `AAAA-Qn`. `reportingMonth` usa `YearMonth` para validação de calendário, preservando a string do contrato existente. Medições de energia, massa, carbono e percentuais permanecem `double`: são quantidades aproximadas, não saldos monetários ou valores contábeis que exigem decimal exato. Percentuais são limitados a 0–100 e valores não finitos são rejeitados. Uma futura regra de arredondamento/regulação deve definir escala e migração antes de adotar `BigDecimal`.

`MongoSchemaInitializer` cria apenas os índices usados: `environmental_license.licenseNumber` único (invariante de negócio) e `environmental_license.expirationDate` (janela de renovação). A ordenação de listas por ID usa o índice `_id` nativo. Índices históricos de campos não consultados não são criados em bancos novos; bancos existentes podem conservá-los. Remova cada índice antigo em janela planejada depois de inspecionar `getIndexes()`, carga e eventual uso externo. A inicialização não apaga índices automaticamente. Mudanças futuras de campo devem seguir: leitura compatível de documento antigo, backfill idempotente testado, então exigência do novo campo. Renomeações/alterações semânticas exigem script versionado, backup, validação de duplicatas e plano de rollback. Mongock não foi adicionado para um único backfill simples e idempotente; considere-o quando houver uma sequência real de migrações dependentes.

## Operação e verificação

Mongo usa tempos limite configuráveis de conexão (`APP_MONGO_CONNECT_TIMEOUT`, padrão `PT5S`), leitura (`APP_MONGO_READ_TIMEOUT`, `PT15S`) e seleção de servidor (`APP_MONGO_SERVER_SELECTION_TIMEOUT`, `PT5S`), todos entre 1 e 60 segundos. O pool espera no máximo 5 segundos por conexão. Configuração de segurança, lease ou timeout inválida falha na inicialização; Mongo indisponível falha durante a criação/verificação de índices. Spring concede até 20 segundos para solicitações e tarefas em andamento no encerramento; Compose aguarda 30 segundos antes de forçar a parada. Recursos Mongo são fechados pelo ciclo de vida Spring.

```bash
./mvnw test       # unidade, contratos HTTP e ArchUnit
./mvnw verify     # inclui Testcontainers/Mongo e SpotBugs (alta prioridade)
./mvnw package    # target/ecocity-esg.jar
git archive --format=zip --output=ecocity-esg-university.zip HEAD
```

`git archive` empacota somente arquivos versionados: sem `.git`, `target`, `.env`, logs ou arquivos locais de IDE. O Maven Wrapper fixa Maven 3.9.9 e verifica SHA-256 da distribuição baixada. SpotBugs roda em `verify` para achados de alta prioridade; não há meta artificial de 100% de cobertura. Dependências transitivas permanecem governadas pelo BOM do Spring Boot. Não foi adicionada varredura automática de CVEs baseada em feed remoto: sem uma fonte/API confiável e política de triagem, uma verificação local produziria falhas instáveis; isso deve ser definido antes do futuro CI.

Os testes de integração precisam de Docker. Alternativamente, `-Dtest.mongodb.uri=mongodb://...` aponta para Mongo descartável exclusivo de testes. Spring Boot 3.5.16 é a última versão OSS da linha 3.5; a migração para uma linha com suporte contínuo (4.x) permanece necessária antes de uma operação de longo prazo. O projeto não inclui CI/CD, infraestrutura Azure, observabilidade externa ou plataforma de identidade nesta etapa.
