# Gestão Financeira Pessoal — API REST

API REST para controle de receitas e despesas pessoais, desenvolvida com Java e Spring Boot.
Oferece autenticação segura com JWT, gerenciamento completo de transações e relatórios financeiros.

---

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.6 |
| Spring Security | 7.0.5 |
| Spring Data JPA | 4.0.5 |
| PostgreSQL | 17.6 |
| Flyway | 11.14.1 |
| Springdoc OpenAPI | 2.8.8 |
| Lombok | 1.18.46 |
| Maven | 3.x |

---

## Funcionalidades

- Cadastro e autenticação de usuários com JWT
- CRUD completo de transações financeiras
- Categorização de receitas e despesas
- Relatório de resumo mensal com total de receitas, despesas e saldo
- Relatório de gastos agrupados por categoria
- Documentação interativa via Swagger UI
- Validação de dados com Bean Validation
- Tratamento global de exceções padronizado

---

## Pré-requisitos

- Java 17 ou superior
- PostgreSQL instalado e rodando
- Maven 3.x

---

## Configuração e execução

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/gestao-financeira.git
cd gestao-financeira
```

### 2. Crie o banco de dados

```sql
CREATE DATABASE financas_db;
```

### 3. Configure as variáveis de ambiente

| Variável | Descrição |
|---|---|
| DB_USERNAME | Usuário do PostgreSQL |
| DB_PASSWORD | Senha do PostgreSQL |
| JWT_SECRET | Chave secreta em Base64 (mínimo 256 bits) |
| JWT_EXPIRATION | Tempo de expiração do token em ms (ex: 86400000) |

Para gerar uma chave segura:

```bash
openssl rand -base64 64
```

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

### 5. Acesse a documentação
http://localhost:8080/swagger-ui/index.html

---

## Documentação da API

### Autenticação

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | /auth/cadastro | Cadastrar novo usuário | Não |
| POST | /auth/login | Autenticar e obter token JWT | Não |

### Transações

| Método | Rota | Descrição | Autenticação |
|---|---|---|---|
| POST | /transacoes | Criar transação | Sim |
| GET | /transacoes | Listar transações do usuário | Sim |
| GET | /transacoes/{id} | Buscar transação por ID | Sim |
| PUT | /transacoes/{id} | Editar transação | Sim |
| DELETE | /transacoes/{id} | Remover transação | Sim |
| GET | /transacoes/resumo?mes={ano-mes} | Resumo financeiro mensal | Sim |
| GET | /transacoes/por-categoria | Gastos agrupados por categoria | Sim |

### Exemplo de autenticação no Swagger

1. Acesse `http://localhost:8080/swagger-ui/index.html`
2. Execute `POST /auth/login` e copie o token retornado
3. Clique em **Authorize** no canto superior direito
4. Cole o token e clique em **Authorize**
5. Todos os endpoints passarão a enviar o token automaticamente

---

## Segurança

Todas as rotas exceto `/auth/**` exigem autenticação via token JWT no header:

Authorization: Bearer {token}

Cada usuário tem acesso exclusivo às suas próprias transações. Tentativas de acessar dados de outros usuários retornam `403 Forbidden`.