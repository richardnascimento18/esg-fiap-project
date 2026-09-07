# EcoCity ESG

API REST para gestão ambiental, social e de governança em cidades inteligentes. O projeto registra consumo de energia, coletas de resíduos, emissões de carbono, indicadores de diversidade e licenças ambientais.

Desenvolvido em Java 21, Spring Boot e MongoDB para a atividade da disciplina **Um novo paradigma com Not Only SQL**, pelo Grupo 31.

## Funcionalidades

| Recurso | Coleção no MongoDB | Finalidade |
| --- | --- | --- |
| Consumo de energia | `energy_consumption` | Registrar leituras por instalação e identificar consumo acima do limite. |
| Coleta de resíduos | `waste_collection` | Acompanhar peso coletado, reciclagem e destinação por distrito. |
| Emissões de carbono | `carbon_emission` | Registrar emissões e calcular se a compensação informada é suficiente. |
| Relatórios de diversidade | `diversity_report` | Consolidar indicadores de inclusão e realização de treinamentos por departamento. |
| Licenças ambientais | `environmental_license` | Controlar validade e emitir avisos de renovação no log. |

Cada recurso oferece cadastro, consulta paginada, consulta por identificador, atualização e exclusão.

### Regras de negócio

- **Energia:** `alertTriggered` é verdadeiro somente quando `consumptionKwh > thresholdKwh`. A igualdade não dispara um alerta.
- **Carbono:** `compensated` é verdadeiro quando `compensationTonnes >= emissionTonnes`. O sistema compara os valores informados; não executa uma compensação ambiental externa.
- **Resíduos e diversidade:** os percentuais devem estar entre 0 e 100, inclusive.
- **Licenças:** a data de vencimento não pode ser anterior à data de emissão. Na gravação, o estado passa a `EXPIRED` quando o instante atual é posterior ao vencimento; caso contrário, passa a `ACTIVE`. Um estado `SUSPENDED` fornecido internamente é preservado nessa avaliação.
- **Avisos de renovação:** o caso de uso percorre as licenças em páginas de 100 registros e registra um aviso para aquelas com vencimento estritamente anterior ao instante atual acrescido de 30 dias. Licenças já vencidas também geram avisos; licenças suspensas são excluídas.

O agendamento padrão executa às **06h**, no fuso horário da JVM. Os avisos são escritos no log a cada execução, sem envio de e-mail e sem alteração dos registros. As consultas devolvem o estado armazenado da licença, sem recalculá-lo durante a leitura.

O campo `status` não integra o corpo de cadastro ou atualização de licenças. Uma atualização pela API recalcula o estado a partir das datas, inclusive quando o registro anterior estava suspenso. Esse comportamento foi mantido por compatibilidade.

## Arquitetura

O projeto utiliza Arquitetura Hexagonal, com dependências de código orientadas para o domínio e para as portas da aplicação.

| Pacote, relativo a `com.ecocity.esg` | Responsabilidade |
| --- | --- |
| `domain.model` | Modelos, validações e operações de negócio. |
| `domain.exception` | Exceções de validação e de recurso não encontrado. |
| `application.port.in` | Contratos dos casos de uso expostos aos adaptadores de entrada. |
| `application.port.out` | Contratos de persistência e de emissão de avisos. |
| `application.usecase` | Coordenação das operações, consultas e verificações de vencimento. |
| `adapter.in.web` | Controladores REST, DTOs, mapeadores, tratamento de erros e OpenAPI. |
| `adapter.in.scheduler` | Acionamento periódico da verificação de licenças. |
| `adapter.in.bootstrap` | Carga de exemplos do perfil de desenvolvimento, por meio das portas de entrada. |
| `adapter.out.persistence.mongodb` | Documentos, repositórios Spring Data, mapeadores, índices e configuração do driver. |
| `adapter.out.notification` | Implementação dos avisos por meio de log. |
| `infrastructure.config` | Composição dos serviços e fornecimento do relógio `Clock`. |

Os controladores, o agendador e a carga de exemplos dependem das portas de entrada. Os serviços dependem dos modelos e das portas de saída. Spring, MongoDB, agendamento e registro de logs ficam fora do domínio e dos serviços de aplicação.

As operações CRUD permanecem agrupadas por recurso, pois compartilham o mesmo contexto funcional. A verificação de vencimentos possui uma porta própria, `CheckLicenseExpirationUseCase`, por representar um processo distinto.

### Modelos e atualizações

Os modelos de domínio possuem campos finais e não expõem setters. As operações `forCreation` e `updateWith` aplicam as regras relacionadas à gravação e devolvem novos objetos. A criação ignora um identificador recebido internamente; a atualização preserva o identificador do registro existente.

Os construtores utilizados pelos builders permitem reconstruir o estado persistido sem recalcular indicadores durante uma consulta. Os DTOs HTTP e os documentos MongoDB continuam separados dos modelos de domínio, com mapeamento explícito entre eles. Lombok reduz o código repetitivo de getters e builders; o domínio não depende de Spring ou do driver MongoDB.

O relógio é injetado nos serviços que precisam do instante atual. Isso permite testar limites de validade com um horário fixo.

Testes com **ArchUnit** verificam as dependências entre camadas, o isolamento de MongoDB e agendamento e a ausência de ciclos entre os pacotes principais.

## Persistência

### Escolha do MongoDB

A aplicação foi concebida como um projeto novo, orientado a documentos. Os campos `sensorMetadata`, em energia, e `additionalRequirements`, em licenças, aceitam atributos variáveis conforme o sensor ou o tipo de licença. Os demais campos mantêm contratos explícitos nos DTOs e nas validações.

Essa modelagem permite representar os dados variáveis dentro do próprio registro. A configuração fornecida utiliza uma instância de MongoDB; não configura fragmentação de dados nem um conjunto de réplicas para desenvolvimento.

### Inicialização das coleções e dos índices

`MongoSchemaInitializer` garante os índices necessários diretamente no MongoDB, em cada inicialização e antes da carga de exemplos. A criação dos índices também cria as coleções ausentes. Índices já existentes com a mesma definição são mantidos, assim como os documentos armazenados.

| Coleção | Índices, além de `_id` |
| --- | --- |
| `energy_consumption` | `facilityId` crescente, `readingTimestamp` decrescente, `alertTriggered` crescente. |
| `waste_collection` | `district` crescente, `wasteType` crescente, `collectionDate` decrescente. |
| `carbon_emission` | `sourceFacility`, `reportingPeriod` e `compensated`, todos crescentes. |
| `diversity_report` | `department` e `reportingMonth`, ambos crescentes. |
| `environmental_license` | `licenseNumber` crescente e único; `status` e `expirationDate` crescentes. |

Flyway, H2 e o pool JDBC Hikari foram removidos. O MongoDB é o único banco utilizado pela aplicação. A inicialização não mantém um histórico de migrações nem transforma dados antigos. Se uma definição de índice conflitar com a existente, a aplicação falha na inicialização para que a incompatibilidade seja resolvida explicitamente.

Os nomes das coleções, dos campos persistidos e dos índices foram preservados. Um banco utilizado pela versão anterior pode continuar sendo utilizado, sem reinicialização dos dados. O antigo arquivo de controle H2 deixa de ser consultado.

O pool do driver MongoDB mantém de **5 a 50 conexões**, espera máxima de **5 segundos** e tempo máximo de ociosidade de **60 segundos**. O Undertow utiliza **2 threads de entrada e saída** e **10 threads de trabalho**.

## Requisitos

- JDK 21.
- Maven 3.9 ou superior.
- MongoDB 7, local ou disponibilizado por Docker Compose.
- Docker para a execução automática dos testes de integração com Testcontainers.

## Execução local

Na raiz do projeto, inicie o MongoDB de desenvolvimento:

```bash
docker compose up -d mongodb
```

Em seguida, execute a aplicação:

```bash
mvn spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O perfil padrão é `dev`, com conexão ao banco `ecocity_esg` em `localhost:27017`. As credenciais locais estão definidas em `docker-compose.yml` e `application-dev.yml`.

Para gerar e executar o pacote:

```bash
mvn package
java -jar target/ecocity-esg.jar
```

### Execução da aplicação em contêiner

O perfil `app` do Compose inclui a aplicação e configura a conexão com o serviço MongoDB:

```bash
docker compose --profile app up --build -d
```

A porta HTTP permanece `8080`. A construção da imagem empacota a aplicação sem executar testes; utilize os comandos de verificação descritos abaixo antes da implantação.

### Configuração

| Propriedade | Variável de ambiente | Finalidade |
| --- | --- | --- |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | Selecionar o perfil; o padrão é `dev`. |
| `spring.data.mongodb.uri` | `SPRING_DATA_MONGODB_URI` | Configurar endereço, autenticação e opções de conexão. |
| `spring.data.mongodb.database` | `SPRING_DATA_MONGODB_DATABASE` | Selecionar o banco utilizado pelo Spring Data. |
| `server.port` | `SERVER_PORT` | Alterar a porta HTTP. |
| `app.license-alert.cron` | `APP_LICENSE_ALERT_CRON` | Alterar o agendamento dos avisos; `-` desativa a execução automática. |

Para utilizar outro banco, configure tanto a URI quanto o nome do banco. O projeto não inclui autenticação ou autorização para os endpoints.

### Perfis e dados de exemplo

No perfil `dev`, `DataSeeder` insere **12 registros em cada coleção vazia** por meio dos casos de uso. Coleções que já contêm dados são preservadas, mesmo que possuam menos de 12 registros. A carga serve à demonstração em desenvolvimento e não é uma migração de dados.

O perfil `test` fica em `src/test/resources`. Ele utiliza um banco de testes separado, desativa o agendamento e não executa a carga de exemplos automaticamente. O teste específico da carga a aciona explicitamente.

## Swagger e OpenAPI

Com a aplicação em execução:

- [Swagger UI](http://localhost:8080/swagger-ui.html): documentação interativa para consultar e executar as operações.
- [OpenAPI em JSON](http://localhost:8080/v3/api-docs): especificação dos endpoints, parâmetros e esquemas.
- [Verificação de saúde](http://localhost:8080/actuator/health): estado da aplicação e da conexão com o banco.

A integração utiliza `springdoc-openapi-starter-webmvc-ui` 2.6.0 com Spring Boot 3.3.4, conforme a [matriz de compatibilidade do springdoc](https://springdoc.org/faq.html). As 25 operações possuem descrições, e os indicadores calculados são identificados nos esquemas de resposta.

## Contrato HTTP

### Recursos

| Recurso | Caminho base |
| --- | --- |
| Energia | `/api/v1/energy-consumptions` |
| Resíduos | `/api/v1/waste-collections` |
| Carbono | `/api/v1/carbon-emissions` |
| Diversidade | `/api/v1/diversity-reports` |
| Licenças | `/api/v1/environmental-licenses` |

### Operações disponíveis em cada recurso

| Método | Caminho relativo | Resultado de sucesso |
| --- | --- | --- |
| `POST` | Caminho base | `201 Created`, com o registro criado. |
| `GET` | Caminho base, com `page` e `size` opcionais | `200 OK`, com uma lista JSON. |
| `GET` | `/{id}` | `200 OK`, com o registro encontrado. |
| `PUT` | `/{id}` | `200 OK`, com o registro atualizado. |
| `DELETE` | `/{id}` | `204 No Content`, sem corpo. |

A paginação começa em **0** e utiliza **20 registros por página** por padrão. A resposta de listagem é um array, sem metadados de paginação ou ordenação adicional. O `PUT` substitui os campos editáveis; não funciona como atualização parcial.

Datas são representadas por instantes ISO 8601. Valores nulos são omitidos das respostas. Os nomes dos campos, das enumerações e dos endpoints foram mantidos.

### Exemplo de cadastro de consumo

```bash
curl -X POST http://localhost:8080/api/v1/energy-consumptions \
  -H 'Content-Type: application/json' \
  -d '{
    "facilityId": "FAC-001",
    "facilityName": "Paço Municipal",
    "city": "São Paulo",
    "sourceType": "SOLAR",
    "consumptionKwh": 3500,
    "thresholdKwh": 3000,
    "readingTimestamp": "2026-09-07T12:00:00Z",
    "sensorMetadata": {
      "panelEfficiencyPercentage": 21.5,
      "panelCount": 40
    }
  }'
```

A resposta inclui o identificador gerado e `alertTriggered: true`. O indicador é calculado pelo domínio.

### Erros

| Código | Situação |
| --- | --- |
| `400` | Violação das validações dos campos do corpo da requisição. |
| `404` | Registro não encontrado em consulta, atualização ou exclusão. |
| `422` | Violação de uma regra de domínio, como vencimento anterior à emissão. |
| `500` | Exceção capturada pelo tratamento genérico existente. |

O corpo de erro contém `timestamp`, `status`, `error`, `message` e `path`. Erros de validação de campos também incluem `details`.

As mensagens e a classificação dos erros existentes foram preservadas. Isso inclui o tratamento genérico de JSON malformado e de determinadas falhas de conversão ou persistência como `500`. Uma revisão dessa classificação deve ser tratada como alteração do contrato HTTP.

## Testes e verificação

Execute os testes sem necessidade de MongoDB ou Docker:

```bash
mvn test
```

Essa etapa inclui testes unitários dos serviços e do domínio, regras de arquitetura, contratos HTTP e Swagger. Os testes HTTP dessa etapa utilizam os controladores, mapeadores e serviços reais, com substitutos de teste apenas nas portas de saída. O Mockito usa o mecanismo de subclasses, sem exigir anexação dinâmica de um agente à JVM.

Para executar também os testes de integração com MongoDB real:

```bash
mvn verify
```

O Maven Failsafe executa as classes `*IT`, incluindo os ciclos CRUD, a especificação OpenAPI, a carga de exemplos e a recuperação dos índices após a remoção do banco de teste. O Testcontainers inicia MongoDB 7 em um contêiner descartável. A primeira execução pode baixar a imagem; o Docker deve estar disponível.

Em ambientes com um servidor MongoDB de testes já disponível, é possível informar sua URI explicitamente:

```bash
mvn verify -Dtest.mongodb.uri=mongodb://localhost:27017
```

Nesse modo, o servidor deve ser exclusivo para testes: a suíte utiliza `ecocity_esg_test` e cria e remove bancos temporários com prefixo `schema_test_`. A suíte não apaga o banco de desenvolvimento `ecocity_esg`.

## Tecnologias

Java 21, Spring Boot 3.3.4, Undertow, Spring Data MongoDB, MongoDB 7, Lombok, springdoc-openapi 2.6.0, JUnit 5, Mockito, AssertJ, MockMvc, ArchUnit e Testcontainers.

## Integrantes do Grupo 31

- Richard Domingos Nascimento Junior
- Cesar dos Santos Ribeiro
