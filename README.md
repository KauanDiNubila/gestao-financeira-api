# Gestão Financeira API

API REST para controle de finanças pessoais — gerenciamento de receitas e despesas, com autenticação JWT, relatórios mensais e análise de gastos por categoria. Inclui uma interface web servida pela própria aplicação.

## 🔗 Demo ao vivo

**https://gestao-financeira-api-ss04.onrender.com**

> ⚠️ A aplicação está hospedada no plano gratuito do Render, que coloca o serviço em hibernação após um período de inatividade. Por isso, **a primeira requisição pode levar cerca de um minuto** enquanto o serviço "acorda". As requisições seguintes são rápidas.

## Funcionalidades

- Cadastro e autenticação de usuários com JWT
- Registro de transações (receitas e despesas) com categoria e data
- Listagem paginada de transações
- Resumo mensal: total de receitas, despesas e saldo
- Sumarização de despesas por categoria
- Cada usuário acessa apenas as próprias transações
- Interface web para interagir com a API sem ferramentas externas

## Tecnologias

- Java 17
- Spring Boot 4
- Spring Security + JWT (jjwt)
- Spring Data JPA
- PostgreSQL
- Flyway (versionamento de banco)
- Swagger / OpenAPI (springdoc)
- JUnit 5 + Mockito (testes)
- Docker (multi-stage build)
- Maven

## Arquitetura

O projeto segue uma organização em camadas com responsabilidades bem definidas:

```
br.com.gestao
├── config       → segurança, filtro JWT, Swagger
├── controller   → endpoints REST
├── domain       → entidades e repositórios (Transacao, Usuario)
├── dto          → objetos de request e response
├── exception    → exceções específicas e handler global
└── service      → regras de negócio
```

O tratamento de erros usa exceções específicas mapeadas para os status HTTP corretos (404 para recurso não encontrado, 403 para acesso negado, 409 para conflito de regra de negócio), em vez de respostas genéricas.

## Endpoints principais

### Autenticação

```
POST /auth/cadastro   → cria um usuário
POST /auth/login      → autentica e retorna o token JWT
```

### Transações (requer autenticação)

```
GET    /transacoes              → lista paginada (?page=0&size=10&sort=data,desc)
POST   /transacoes              → cria uma transação
GET    /transacoes/{id}         → busca por id
PUT    /transacoes/{id}         → atualiza
DELETE /transacoes/{id}         → remove
GET    /transacoes/resumo?mes=AAAA-MM   → resumo mensal (receitas, despesas, saldo)
GET    /transacoes/por-categoria        → total de despesas agrupado por categoria
```

### Documentação interativa

```
GET /swagger-ui.html   → Swagger UI
```

## Testes

```bash
mvn test
```

Os testes de unidade cobrem as regras de negócio do `TransacaoService` com JUnit 5 e Mockito: cálculo do resumo mensal, caso sem transações, sumarização por categoria ordenada, e as validações de autorização (acesso negado a transação de outro usuário) e de recurso não encontrado.

## Como rodar localmente

### 1. Subir o banco de dados (PostgreSQL no Docker)

```bash
docker run --name postgres-financas -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres123 -e POSTGRES_DB=financas_db -e POSTGRES_HOST_AUTH_METHOD=md5 -p 5432:5432 -d postgres
```

### 2. Configurar as variáveis de ambiente

A aplicação lê as credenciais de variáveis de ambiente. Defina:

```
DB_PASSWORD=postgres123
JWT_SECRET=uma_chave_base64_valida_de_pelo_menos_64_caracteres
```

> O `JWT_SECRET` deve ser uma string Base64 válida (sem o caractere `_`), pois é decodificado como Base64 na geração do token.

As variáveis de URL e usuário do banco têm valores padrão para desenvolvimento local (`localhost`/`postgres`), então não precisam ser definidas para rodar na sua máquina.

### 3. Rodar a aplicação

Pela IDE (executando a classe `GestaoApplication`) ou via Maven:

```bash
mvn spring-boot:run
```

Acesse a interface em **http://localhost:8080**.

## Deploy

A aplicação está containerizada com um `Dockerfile` multi-stage (compila o `.jar` e o executa numa imagem JRE enxuta) e implantada no Render, com banco PostgreSQL gerenciado e variáveis de ambiente para as credenciais. O frontend é servido pela própria aplicação Spring Boot, em uma única origem.