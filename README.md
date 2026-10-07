# Delivery System — Java Advanced

Sistema de pedidos de delivery desenvolvido com arquitetura de microsserviços para a disciplina de Java Advanced da FIAP.

## Integrantes

| Nome | RM |
|---|---|
| Felipe Augusto Lopes Ferreira | 563982 |
| Kaique Mascarenhas dos Santos | 565802 |

## Tecnologias Utilizadas

- Java 25
- Spring Boot 4
- Spring Cloud
- Netflix Eureka
- Spring Cloud LoadBalancer
- Spring Retry
- Spring Data JPA
- H2 Database
- RabbitMQ
- Spring AI
- Ollama (llama3.2)
- Gradle
- Docker Compose

## Arquitetura do Projeto

O sistema possui quatro microsserviços independentes:

| Microsserviço | Porta | Responsabilidade |
|---|---|---|
| eureka-server | 8761 | Registro e descoberta dos microsserviços |
| order-service | 8080 | Gerenciamento de pratos, pedidos, avaliações e assistente de IA |
| payment-service | 8081 e 8082 | Simulação de pagamentos com falhas aleatórias |
| review-service | 8083 | Consumo de avaliações, processamento em lote e ranking |

Cada microsserviço possui seu próprio projeto Gradle.

## Funcionalidades

### 1. Eureka Server

O Eureka Server é responsável pelo registro e descoberta dos microsserviços.

Os serviços utilizam seus nomes registrados para realizar a comunicação, evitando endereços fixos entre microsserviços.

**Painel do Eureka:**

http://localhost:8761

### 2. Order Service

Responsável pelo gerenciamento do cardápio e dos pedidos.

O banco H2 é inicializado com cinco pratos.

**Endpoints:**

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /dishes | Lista todos os pratos |
| GET | /dishes/{id} | Consulta um prato pelo ID |
| POST | /orders | Cria um pedido |
| GET | /orders/{id} | Consulta um pedido pelo ID |

O processamento dos pedidos utiliza:

- Transações com `@Transactional`.
- Bloqueio pessimista com `PESSIMISTIC_WRITE`.
- Validação de quantidade e estoque.
- Comunicação com o serviço de pagamentos.
- Atualização do estoque após pagamento aprovado.

**Exemplo de criação de pedido:**

```http
POST http://localhost:8080/orders
Content-Type: application/json

{
  "dishId": 1,
  "quantity": 1
}
```

**Códigos HTTP utilizados:**

- `201 Created`: pedido confirmado.
- `400 Bad Request`: quantidade inválida.
- `404 Not Found`: prato inexistente.
- `409 Conflict`: estoque insuficiente.
- `502 Bad Gateway`: falha no pagamento.

### 3. Payment Service

O Payment Service simula o processamento de pagamentos.

São utilizadas duas instâncias:

- Instância 1: porta 8081.
- Instância 2: porta 8082.

O serviço apresenta aproximadamente 50% de probabilidade de falha em cada tentativa.

**Endpoint:**

```http
POST http://localhost:8081/payments
Content-Type: application/json

{
  "amount": 79.80
}
```

**Exemplo de resposta aprovada:**

```json
{
  "status": "APPROVED",
  "instance": 8081
}
```

O Order Service utiliza:

- Descoberta de serviços pelo Eureka.
- Balanceamento de carga com Spring Cloud LoadBalancer.
- Retry com Spring Retry.
- Backoff exponencial.
- Jitter para variar os intervalos entre tentativas.
- Configurações de retry no `application.properties`.

A comunicação utiliza o endereço lógico:

```text
http://PAYMENT-SERVICE/payments
```

### 4. RabbitMQ

O RabbitMQ é utilizado para comunicação assíncrona entre os microsserviços.

**Configuração:**

| Recurso | Nome |
|---|---|
| Exchange | delivery.exchange |
| Tipo | Topic |
| Queue | reviews.queue |
| Routing Key | reviews.new |

A fila é durável.

**Endpoint para enviar avaliações:**

```http
POST http://localhost:8080/reviews
Content-Type: application/json

{
  "dishId": 1,
  "rating": 5,
  "comment": "Muito bom!"
}
```

**Resposta:**

```text
202 Accepted
```

O Order Service valida a avaliação e publica uma mensagem JSON no RabbitMQ.

O armazenamento e processamento das avaliações são realizados pelo Review Service.

### 5. Review Service

Responsável por consumir e processar as avaliações recebidas pelo RabbitMQ.

O serviço utiliza:

- `@RabbitListener` para consumir mensagens.
- `ConcurrentHashMap` para acumular avaliações.
- `@Scheduled` para processamento periódico.
- Banco H2 para armazenar os resumos das avaliações.

O processamento em lote ocorre a cada cinco segundos.

Para cada prato, são armazenados:

- ID do prato.
- Nome do prato.
- Quantidade de avaliações.
- Soma das notas.

A média é calculada a partir da soma das notas e da quantidade de avaliações.

**Endpoint:**

```http
GET http://localhost:8083/reviews/ranking
```

**Exemplo de resposta:**

```json
[
  {
    "dishId": 1,
    "dishName": "House Burger",
    "average": 4.0,
    "count": 3
  }
]
```

O ranking é ordenado pela média das avaliações, da maior para a menor.

### 6. Assistente com Inteligência Artificial

O Order Service disponibiliza um assistente de atendimento utilizando Spring AI e Ollama.

**Modelo utilizado:**

```text
llama3.2
```

O assistente:

- Responde em português.
- Utiliza os dados atuais do cardápio.
- Consulta nomes, preços e estoques dos pratos.
- Responde de forma curta e educada.
- Recusa perguntas que não estejam relacionadas ao restaurante.

**Endpoint:**

```http
POST http://localhost:8080/assistant
Content-Type: application/json

{
  "question": "Qual é o prato mais barato do cardápio?"
}
```

**Formato da resposta:**

```json
{
  "answer": "O prato mais barato do cardápio é o Chocolate Cake, por R$ 18,90."
}
```

## Como Executar o Projeto

### Pré-requisitos

- Java JDK 25.
- Docker Desktop.
- Ollama instalado.
- Modelo llama3.2 disponível.
- Portas 8761, 8080, 8081, 8082, 8083, 5672 e 15672 disponíveis.

### 1. Clonar o repositório

```bash
git clone https://github.com/FelipeAugusto99/delivery-system.git
cd delivery-system
```

### 2. Iniciar o RabbitMQ

Na pasta principal do projeto:

```bash
docker compose up -d
```

**Painel do RabbitMQ:**

http://localhost:15672

**Credenciais locais:**

```text
Usuário: guest
Senha: guest
```

### 3. Preparar o Ollama

Baixe o modelo utilizado pelo projeto:

```bash
ollama pull llama3.2
```

Verifique se o Ollama está em execução na porta 11434.

### 4. Iniciar o Eureka Server

Abra um terminal:

```powershell
cd eureka-server
.\gradlew.bat bootRun
```

### 5. Iniciar o Payment Service

Abra um segundo terminal para a primeira instância:

```powershell
cd payment-service
.\gradlew.bat bootRun --args="--server.port=8081"
```

Abra um terceiro terminal para a segunda instância:

```powershell
cd payment-service
.\gradlew.bat bootRun --args="--server.port=8082"
```

As duas instâncias devem aparecer registradas no Eureka.

### 6. Iniciar o Order Service

Abra outro terminal:

```powershell
cd order-service
.\gradlew.bat bootRun
```

O serviço estará disponível em:

http://localhost:8080

### 7. Iniciar o Review Service

Abra outro terminal:

```powershell
cd review-service
.\gradlew.bat bootRun
```

O serviço estará disponível em:

http://localhost:8083

**Observação:** os comandos acima consideram que cada terminal foi aberto na pasta principal do projeto.

## Testes Realizados

### Registro no Eureka

Foi verificado o registro dos microsserviços no painel do Eureka.

### Balanceamento de Carga

Foram executadas requisições de pagamento utilizando as duas instâncias do Payment Service.

As respostas identificaram as portas 8081 e 8082.

### Retry de Pagamentos

Foram testadas falhas simuladas de pagamento e novas tentativas automáticas.

Quando todas as tentativas falham, o Order Service retorna HTTP 502 e preserva o estoque.

### Concorrência de Pedidos

Foi realizado um teste com:

- Estoque inicial: 10 unidades.
- Requisições concorrentes: 50.
- Pedidos confirmados: 10.
- Estoque final: 0.

O bloqueio pessimista impediu que fossem confirmados pedidos acima do estoque disponível.

### RabbitMQ

Foram realizados testes de publicação e consumo de avaliações.

As mensagens foram recebidas pelo Review Service e processadas em lote.

### Ranking de Avaliações

Foram enviadas três avaliações com nota 4 para o mesmo prato.

**Resultado obtido:**

```text
dishId: 1
dishName: House Burger
average: 4.0
count: 3
```

### Assistente de IA

Foram realizados testes com perguntas sobre o cardápio e perguntas fora do contexto do restaurante.

O assistente respondeu utilizando as informações cadastradas no banco H2 e recusou perguntas não relacionadas ao atendimento.

## Repositório GitHub

https://github.com/FelipeAugusto99/delivery-system

## Disciplina

**Java Advanced — FIAP**

Projeto desenvolvido como atividade acadêmica do segundo semestre.