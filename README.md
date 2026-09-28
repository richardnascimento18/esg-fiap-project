# EcoCity ESG

API REST para registrar indicadores ambientais, sociais e de governança de uma cidade. O projeto usa Java 21, Spring Boot, MongoDB e arquitetura hexagonal para separar regras de domínio, casos de uso e integrações.

## Funcionalidades

- Cadastro, consulta, atualização e exclusão de consumo de energia, coleta de resíduos, emissões de carbono, relatórios de diversidade e licenças ambientais.
- Validação de entrada e de domínio, incluindo campos obrigatórios, datas, valores finitos e faixas permitidas.
- HTTP Basic com papéis `EDITOR` e `ADMIN`; credenciais fornecidas por configuração externa.
- Controle de concorrência otimista com versão MongoDB, ETags fortes e `If-Match` obrigatório para alterações de registros existentes.
- `Idempotency-Key` opcional em POST para repetir criações com segurança.
- Cálculo do estado efetivo das licenças e verificação de vencimentos nos próximos 30 dias; agendamento coordenado por lease distribuído no MongoDB. O adaptador de alerta atual registra logs.
- Paginação de coleções, correlação de requisições por `X-Request-ID`, endpoints de saúde, documentação OpenAPI e testes automatizados.

## Tecnologias

| Área | Tecnologias |
| --- | --- |
| Aplicação | Java 21, Spring Boot 3.5.16, Spring Web com Undertow, Validation, Security, Data MongoDB e Actuator |
| Dados | MongoDB 7 no ambiente local; Azure DocumentDB for MongoDB na infraestrutura de nuvem |
| Build e qualidade | Maven Wrapper 3.9.9, JUnit 5, Testcontainers, ArchUnit e SpotBugs |
| API | springdoc OpenAPI 2.8.16 e Swagger UI |
| Contêineres e entrega | Docker, Docker Compose, GitHub Actions e GHCR |
| Infraestrutura | Terraform, Azure Container Apps, Log Analytics, Key Vault e identidades gerenciadas |

## Arquitetura

`src/main/java/com/ecocity/esg/` organiza o domínio em `domain/`, os casos de uso e portas em `application/`, e as entradas e integrações em `adapter/`: controladores REST e agendador recebem chamadas; persistência MongoDB e alerta por log implementam portas de saída. `infrastructure/` reúne a configuração da aplicação. Domínio e casos de uso não dependem dos adaptadores; testes ArchUnit verificam essas fronteiras.

## API

As cinco coleções usam o prefixo `/api/v1`:

| Recurso | Caminho |
| --- | --- |
| Consumo de energia | `/api/v1/energy-consumptions` |
| Coleta de resíduos | `/api/v1/waste-collections` |
| Emissões de carbono | `/api/v1/carbon-emissions` |
| Relatórios de diversidade | `/api/v1/diversity-reports` |
| Licenças ambientais | `/api/v1/environmental-licenses` |

| Método | Caminho relativo à coleção | Resultado |
| --- | --- | --- |
| POST | `/` | Cria registro; retorna `201` e ETag. |
| GET | `/` | Retorna uma lista JSON, com `page` e `size`. |
| GET | `/{id}` | Retorna registro e ETag; `404` se ausente. |
| PUT | `/{id}` | Substitui campos editáveis; exige `If-Match` e retorna nova ETag. |
| DELETE | `/{id}` | Exige `If-Match`; retorna `204`. |

Nas listas, `page` começa em 0 e `size` aceita de 1 a 100 (padrão 20); a ordenação é por ID. A resposta é uma lista, sem envelope de total ou ETag. Paginação por deslocamento pode mudar durante escritas simultâneas.

POST aceita `Idempotency-Key` opcional de 1 a 128 caracteres alfanuméricos ASCII, `.`, `_` ou `-`. A chave vale para toda a coleção, inclusive entre usuários: repetir a mesma criação lógica recupera a entidade; reutilizá-la com conteúdo diferente retorna `409`. A reserva persiste após exclusão e impede recriar com a mesma chave. Sem chave, cada POST é uma nova tentativa de criação.

POST e GET por ID fornecem ETags fortes. Para PUT e DELETE, envie a ETag atual em `If-Match`; as licenças incluem também o estado efetivo na ETag, que pode mudar com o vencimento. Cabeçalho ausente retorna `428`, formato inválido ou ETag fraca retorna `400`, ETag desatualizada retorna `412` e uma escrita simultânea detectada pelo MongoDB pode retornar `409`. Outros resultados relevantes incluem `400` para entrada inválida, `422` para regra de domínio, `401` para credencial ausente/incorreta e `403` para privilégio insuficiente.

Com a aplicação ativa, a interface Swagger está em `/swagger-ui.html`, o documento OpenAPI em `/v3/api-docs` e a saúde em `/actuator/health`, `/actuator/health/liveness` e `/actuator/health/readiness`. Readiness inclui a verificação do MongoDB. O Actuator expõe apenas `health` e `info`, sem detalhes internos de saúde.

## Autenticação e autorização

| Ação | Público | `EDITOR` | `ADMIN` |
| --- | :---: | :---: | :---: |
| GET dos recursos, Swagger/OpenAPI e saúde | Sim | Sim | Sim |
| POST e PUT dos recursos | Não | Sim | Sim |
| DELETE dos recursos e `/actuator/info` | Não | Não | Sim |

As duas contas HTTP Basic são carregadas de variáveis de ambiente. Nomes e senhas precisam ser distintos; cada senha deve ter pelo menos 12 caracteres e não pode ser a senha de exemplo. A aplicação falha na inicialização se a configuração for inválida. As senhas são codificadas com BCrypt em memória; não devem ser commitadas. Em ambientes compartilhados ou na nuvem, use HTTPS na entrada e gerencie e rotacione as credenciais externamente. Essas contas locais não oferecem auditoria individual nem integração com diretório corporativo.

## Execução local

É necessário Docker com Compose para o caminho principal. Para rodar Maven ou a JVM no host, também é necessário Java 21; o Maven Wrapper baixa a distribuição na primeira execução.

```bash
cp .env.example .env
# Edite .env: substitua as quatro senhas de exemplo por senhas locais distintas.
docker compose up --build -d
curl --retry 12 --retry-delay 5 --retry-connrefused -fi http://localhost:8080/actuator/health/readiness
curl -i 'http://localhost:8080/api/v1/waste-collections?page=0&size=20'
```

Exemplo de criação: substitua `SENHA_LOCAL_DO_EDITOR` pela senha configurada em `.env`.

```bash
curl -i -u 'editor:SENHA_LOCAL_DO_EDITOR' \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: exemplo-local-1' \
  -d '{"district":"Centro","wasteType":"RECYCLABLE","weightKg":10,"recyclingRatePercentage":50,"collectionDate":"2026-01-01T00:00:00Z","collectorTeam":"Equipe","properlyDisposed":true}' \
  http://localhost:8080/api/v1/waste-collections
```

O Compose publica a API na porta `API_PORT` (padrão 8080) e o MongoDB **somente em `127.0.0.1`**, na porta `MONGO_PORT` (padrão 27017). O usuário Mongo da aplicação tem acesso `readWrite` ao banco `ecocity_esg`; o script de criação desse usuário executa apenas em volume novo. O perfil `dev` só insere dados demonstrativos quando `APP_SEED_DEMO_DATA=true`. No Compose, o agendador de licenças fica desligado.

```bash
docker compose down           # preserva o volume MongoDB
docker compose down --volumes # apaga também os dados locais
```

Para executar a JVM no host, mantenha o MongoDB disponível (por exemplo, `docker compose up -d mongodb`), configure uma URI válida com a senha codificada para URL quando necessário e execute:

```bash
export SPRING_DATA_MONGODB_URI='mongodb://USUARIO:SENHA_CODIFICADA@localhost:27017/ecocity_esg?authSource=ecocity_esg'
export APP_EDITOR_USERNAME='editor'
export APP_EDITOR_PASSWORD='SENHA_LOCAL_DO_EDITOR'
export APP_ADMIN_USERNAME='admin'
export APP_ADMIN_PASSWORD='SENHA_LOCAL_DO_ADMIN'
./mvnw spring-boot:run
```

O perfil base deixa o agendamento desligado (`APP_LICENSE_ALERT_CRON=-`). Se ativado, `APP_LICENSE_ALERT_LEASE` controla a duração do lease (padrão `PT1M`, de 15 segundos a 1 hora). A aplicação também aceita `APP_MONGO_CONNECT_TIMEOUT`, `APP_MONGO_READ_TIMEOUT` e `APP_MONGO_SERVER_SELECTION_TIMEOUT` para tempos limite MongoDB.

## Testes e qualidade

```bash
./mvnw test         # testes unitários, contratos HTTP e ArchUnit
./mvnw verify       # acrescenta testes de integração e análise SpotBugs
./mvnw package      # gera target/ecocity-esg.jar, sem as etapas de verify
```

Os testes de integração usam Testcontainers com MongoDB 7 e precisam de Docker disponível. `verify` executa as fases de teste e integração do Maven, além do SpotBugs com limite para achados de prioridade alta. Os testes ArchUnit verificam dependências entre camadas. O Wrapper fixa Maven 3.9.9.

## Contêineres e CI/CD

O Dockerfile constrói o JAR em uma etapa Maven e executa a aplicação em uma imagem Java 21 sem usuário root. Em PRs para `staging` e `production`, o CI roda `./mvnw clean verify` e constrói a imagem; PRs para `production` devem vir da branch `staging` deste repositório.

Em push para `staging`, o workflow verifica o projeto, publica no GHCR uma tag imutável `sha-<SHA completo>` e, em um runner novo, baixa o digest publicado para um smoke test com MongoDB. Se a tag já existe, reutiliza seu digest sem reconstrução. Após merge de `staging` em `production`, a promoção usa esse mesmo manifesto, atualiza o alias `:production` e verifica igualdade dos digests, sem novo build. Os jobs de implantação Azure usam GitHub OIDC, sem segredo estático de cliente Azure.

## Reprodução no Azure

A implantação acadêmica foi **desativada intencionalmente** após validação e coleta das evidências. O repositório não depende de recursos Azure continuamente ativos, e este documento não pressupõe endpoints de nuvem em funcionamento.

O Terraform em `infra/` define um backend remoto separado para o estado, um grupo de recursos compartilhado, grupos próprios para `staging` e `production`, Azure DocumentDB for MongoDB, Log Analytics, ambientes e aplicações Container Apps, Key Vault e identidades gerenciadas atribuídas pelo usuário. Credenciais federadas ligam os ambientes GitHub às identidades de implantação via OIDC. Uma função personalizada concede apenas as permissões necessárias para atualizar e inspecionar as aplicações. Senhas e URI do banco são geradas ou mantidas fora do código, mas o estado Terraform pode conter dados sensíveis e requer proteção.

Sequência resumida para uma nova implantação:

1. Autentique-se no Azure com uma conta autorizada e revise região, SKUs, cotas e políticas da assinatura. A região configurada em `infra/variables.tf` é restrita e pode exigir adaptação antes do plano.
2. Crie **separadamente** o grupo, a conta de armazenamento e o contêiner do backend remoto; confira `infra/backend.tf` e configure os novos dados do backend localmente. Revise também `infra/locals.tf` para o repositório GitHub e a imagem de bootstrap da nova implantação. Não versione identificadores ou segredos locais.
3. No diretório `infra/`, forneça `TF_VAR_subscription_id` externamente e execute `terraform init`, `terraform plan` e `terraform apply`. O plano deve ser revisado antes da aplicação.
4. Configure, em **cada** ambiente GitHub (`staging` e `production`), os três secrets `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` e `AZURE_SUBSCRIPTION_ID` correspondentes à identidade federada e à assinatura. Defina a variável **do repositório** `AZURE_DEPLOY_ENABLED` como a string exata `true`.
5. Siga o fluxo normal `staging` → `production`. As imagens são construídas e validadas antes da implantação por digest.

Sem `AZURE_DEPLOY_ENABLED=true` (inclusive se a variável não existir), **apenas** `deploy_staging` e `deploy_production` são ignorados. CI de PR, verificação/publicação/smoke no GHCR, promoção e comparação dos digests continuam ativos. Com a variável em `true` e recursos e secrets disponíveis, os jobs Azure seguem o fluxo OIDC existente. Para desmontar novamente, destrua primeiro a pilha de aplicação Terraform; remova por último o backend criado separadamente, após tratar o estado remoto.

## Pacote para entrega universitária

```bash
git archive --format=zip --output=ecocity-esg-university.zip HEAD
```

O arquivo contém apenas arquivos rastreados no commit: deixa de fora `.git`, `target`, `.env` local, estado e planos Terraform, dados de IDE e outros artefatos de execução.
