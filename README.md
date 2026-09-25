# EcoCity ESG

Aplicação Java 21 e Spring Boot 3.3 para registrar indicadores ambientais, sociais e de governança em MongoDB. O projeto mantém domínio, casos de uso e portas independentes de Spring; controladores REST, agendamento, logs e persistência são adaptadores. Testes ArchUnit verificam essas fronteiras.

## Recursos e regras

| Recurso | Rota `/api/v1` | Regra principal |
| --- | --- | --- |
| Energia | `/energy-consumptions` | Alerta quando consumo excede o limite. |
| Resíduos | `/waste-collections` | Peso não negativo e taxa de reciclagem de 0 a 100%. |
| Carbono | `/carbon-emissions` | Compensado quando compensação cobre a emissão; período `AAAA-Q1` a `AAAA-Q4`. |
| Diversidade | `/diversity-reports` | Percentuais de 0 a 100%; mês `AAAA-MM`. |
| Licenças | `/environmental-licenses` | Vencimento não anterior à emissão; número único. |

Cada recurso aceita `POST`, `GET`, `GET /{id}`, `PUT /{id}` e `DELETE /{id}`. As listas retornam arrays JSON e usam `page` (a partir de 0) e `size` (1 a 100, padrão 20), ordenados por identificador. O offset pode deslocar itens entre páginas durante escritas simultâneas; clientes que precisam de um retrato consistente devem evitar paginar enquanto alteram a coleção.

Validações de entrada retornam `400`, regras de domínio `422`, ausência de registro `404`, conflito de número de licença `409` e falhas inesperadas `500` com mensagem genérica. O corpo de erro contém `timestamp`, `status`, `error`, `message` e `path`; erros de campos incluem `details`. `X-Request-ID` é devolvido em cada resposta e incluído nos logs. A especificação OpenAPI fica em `/v3/api-docs` e a interface em `/swagger-ui.html`.

## Execução local

Requisitos: **JDK 21** e Docker para MongoDB e testes de integração. O Maven Wrapper fixa Maven 3.9.9; não é necessário instalar Maven separadamente.

```bash
cp .env.example .env
# Substitua as duas senhas de exemplo em .env
docker compose up --build -d
```

A API estará em `http://localhost:8080`; MongoDB escuta apenas em `127.0.0.1:27017`. O Compose define dois serviços, uma rede `ecocity`, volume nomeado `mongodb_data` e variáveis para usuários, senhas, portas e carga de demonstração. O usuário da aplicação possui apenas `readWrite` no banco `ecocity_esg`; a conta root serve à inicialização local. A criação desse usuário ocorre **somente no primeiro início de um volume vazio**. Para reutilizar um volume antigo, crie o usuário da aplicação nesse banco com as credenciais adequadas; não apague dados existentes para aplicar a configuração nova.

Para desenvolver com a JVM no host, inicie apenas MongoDB e configure uma URI para o usuário da aplicação:

```bash
docker compose up -d mongodb
SPRING_PROFILES_ACTIVE=dev SPRING_DATA_MONGODB_URI='mongodb://ecocity_app:SENHA@localhost:27017/ecocity_esg?authSource=ecocity_esg' ./mvnw spring-boot:run
```

A URI acima é apenas um formato; substitua `SENHA` pelo valor local e codifique caracteres especiais de URL. Não registre senhas reais no repositório ou em scripts. O perfil base não ativa `dev`. A imagem também não define perfil. Use `SPRING_PROFILES_ACTIVE` e `SPRING_DATA_MONGODB_URI` no ambiente de execução. `dev` usa MongoDB local por padrão; produção deve receber sua própria URI e credenciais externamente.

`DataSeeder` exige **ambos** `SPRING_PROFILES_ACTIVE=dev` e `APP_SEED_DEMO_DATA=true`. Ele insere 12 exemplos por coleção vazia e não roda em outros perfis. O Compose usa `dev`, mas mantém a carga desativada por padrão. O agendamento de avisos fica desligado por padrão (`APP_LICENSE_ALERT_CRON=-`). Defina uma expressão cron de seis campos somente em uma instância escolhida para produzir avisos diários no log.

## Testes e pacote

```bash
./mvnw test       # domínio, casos de uso, HTTP, OpenAPI e ArchUnit; sem Docker
./mvnw verify     # inclui integração com MongoDB 7 via Testcontainers
./mvnw package    # gera target/ecocity-esg.jar
```

O teste de integração pode usar um MongoDB descartável externo com `-Dtest.mongodb.uri=mongodb://...`; essa instância deve ser exclusiva para testes. O arquivo `src/test/resources/docker-java.properties` fixa a versão da API Docker usada por Testcontainers para compatibilidade com Docker 29. O projeto não impõe percentuais artificiais de cobertura nem ferramentas estáticas adicionais; a suíte e as regras de arquitetura são as verificações principais.

## Operação e evolução de dados

Actuator expõe apenas `health` e `info`; `/actuator/health/liveness` e `/actuator/health/readiness` permitem verificar o processo e sua dependência de MongoDB. Detalhes internos de saúde não são enviados a clientes. A aplicação encerra solicitações em andamento de forma graciosa. A inicialização falha quando os índices exigidos não podem ser criados. A exceção inesperada é registrada com stack trace e método/caminho HTTP, enquanto a resposta pública permanece genérica. Métricas básicas de Spring Boot e MongoDB existem internamente, sem endpoint público de métricas.

`MongoSchemaInitializer` garante índices na inicialização, incluindo unicidade de `licenseNumber` e índice de `expirationDate` para a busca de renovação. Alterações futuras em documentos devem primeiro permitir leitura de campos antigos ausentes, depois preencher dados existentes com um script versionado e testado, e só então tornar o campo obrigatório. Mudanças de unicidade ou índices exigem verificação de duplicatas e planejamento de rollback antes da implantação; o inicializador não migra documentos nem remove índices antigos. Mapas `sensorMetadata` e `additionalRequirements` são deliberadamente flexíveis; clientes devem usar valores JSON pequenos e evitar dados sensíveis.

O status de licença é calculado com um `Clock` injetado: `SUSPENDED` prevalece; após o vencimento a leitura mostra `EXPIRED`; antes dele um estado persistido `RENEWAL_IN_PROGRESS` é preservado. A API atual não fornece comando para iniciar renovação ou suspensão; esses estados podem existir em documentos legados. `PUT` substitui campos editáveis e preserva esses estados quando presentes. Consultas de renovação filtram datas no MongoDB e não alteram documentos. Avisos são logs repetíveis, sem envio externo ou efeito transacional.

O índice único do MongoDB decide corridas de criação da mesma licença; o conflito retorna `409`. As demais atualizações completas por `PUT` aceitam a política de última gravação prevalecer. Não há token de versão/ETag no contrato atual. Para um futuro fluxo colaborativo de edição, introduza pré-condições HTTP e controle de versão do documento conjuntamente, com plano de migração de documentos antigos. Não foram adicionados retries automáticos: falhas permanentes devem permanecer visíveis e os avisos em log podem ser executados novamente no próximo agendamento. Execute o agendador em uma única instância para evitar logs duplicados.

**Limitação de segurança:** a API não implementa autenticação ou autorização. É adequada à demonstração local e deve ficar atrás de controles de acesso de rede em qualquer implantação compartilhada. Credenciais vêm do ambiente; o contêiner da aplicação roda como usuário não root. Não publique o serviço MongoDB fora do host de desenvolvimento nem exponha endpoints internos de gerenciamento.
